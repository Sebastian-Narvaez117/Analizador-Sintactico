package com.analizadorsintactico.minicompilador.parser;

import com.analizadorsintactico.minicompilador.model.Nodo;
import com.analizadorsintactico.minicompilador.model.TipoToken;
import com.analizadorsintactico.minicompilador.model.Token;

import java.util.List;

/**
 * Parser descendente recursivo.
 *
 * GLC soportada:
 *
 *   Programa    → ListaSent FIN
 *   ListaSent   → Sentencia ListaSent | ε
 *   Sentencia   → Declaracion
 *               | IfSent
 *               | WhileSent
 *               | ForSent
 *               | ReturnSent
 *               | PrintSent
 *               | AsignacionSent
 *               | ExprSent          (llamada a función sola, e.g. print(...); )
 *   Declaracion → TipoDato ID [ = Expr ] ;
 *   IfSent      → if ( Cond ) Bloque [ else Bloque ]
 *   WhileSent   → while ( Cond ) Bloque
 *   ForSent     → for ( ForInit ; Cond ; ForUpdate ) Bloque
 *   ForInit     → Declaracion_sin_punto | AsignID | ε
 *   ReturnSent  → return [ Expr ] ;
 *   PrintSent   → print ( Expr ) ;
 *   Bloque      → { ListaSent }
 *   Cond        → Expr [ OpRelLog Expr ]*
 *   Expr        → Termino ( (+|-) Termino )*
 *   Termino     → Factor ( (*|/|%) Factor )*
 *   Factor      → ( Expr ) | -Factor | !Factor | ID [ ++ | -- | ( ArgList ) ] | Literal
 *   ArgList     → ε | Expr (, Expr)*
 *   Literal     → NUMERO | LITERAL_BOOL | LITERAL_NULO | LITERAL_CADENA | LITERAL_CHAR
 *
 * Los tokens COMENTARIO_LINEA, COMENTARIO_BLOQUE y DIRECTIVA se saltan
 * automáticamente durante el parseo.
 */
public class Parser {

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos    = 0;
        saltarNoSignificativos();
    }

    // ════════════════════════════════════════════════════════════════ //
    //  PUNTO DE ENTRADA
    // ════════════════════════════════════════════════════════════════ //

    public Nodo parsearPrograma() {
        Nodo raiz = new Nodo("Programa");
        raiz.agregarHijo(parsearListaSent());
        raiz.agregarHijo(consumir(TipoToken.FIN));
        return raiz;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  LISTA DE SENTENCIAS
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearListaSent() {
        Nodo lista = new Nodo("ListaSent");
        while (puedeIniciarSentencia(actual().tipo)) {
            lista.agregarHijo(parsearSentencia());
        }
        if (lista.hijos.isEmpty()) {
            lista.agregarHijo(new Nodo("ε", true));
        }
        return lista;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  SENTENCIA
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearSentencia() {
        Nodo  sent = new Nodo("Sentencia");
        Token t    = actual();

        if (esTipoDato(t.tipo)) {
            sent.agregarHijo(parsearDeclaracion());

        } else if (t.tipo == TipoToken.IF) {
            sent.agregarHijo(parsearIf());

        } else if (t.tipo == TipoToken.WHILE) {
            sent.agregarHijo(parsearWhile());

        } else if (t.tipo == TipoToken.FOR) {
            sent.agregarHijo(parsearFor());

        } else if (t.tipo == TipoToken.RETURN) {
            sent.agregarHijo(parsearReturn());

        } else if (t.tipo == TipoToken.PRINT) {
            sent.agregarHijo(parsearPrint());

        } else if (t.tipo == TipoToken.ID) {
            sent.agregarHijo(parsearSentenciaID());

        } else {
            throw new RuntimeException(
                "Error sintáctico en línea " + t.linea +
                ": Token inesperado [" + t.tipo + " → \"" + t.valor + "\"]"
            );
        }
        return sent;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  DECLARACIÓN  TipoDato ID [ = Expr ] ;
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearDeclaracion() {
        Nodo decl = new Nodo("Declaracion");
        decl.agregarHijo(consumir(actual().tipo));   // tipo
        decl.agregarHijo(consumir(TipoToken.ID));    // nombre

        // Inicializador opcional
        if (actual().tipo == TipoToken.ASIGNACION) {
            decl.agregarHijo(consumir(TipoToken.ASIGNACION));
            decl.agregarHijo(parsearExpr());
        }
        decl.agregarHijo(consumir(TipoToken.PUNTOCOMA));
        return decl;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  IF  →  if ( Cond ) Bloque [ else Bloque ]
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearIf() {
        Nodo n = new Nodo("IfSent");
        n.agregarHijo(consumir(TipoToken.IF));
        n.agregarHijo(consumir(TipoToken.PARENABRE));
        n.agregarHijo(parsearCondicion());
        n.agregarHijo(consumir(TipoToken.PARENCIERRE));
        n.agregarHijo(parsearBloque());
        if (actual().tipo == TipoToken.ELSE) {
            n.agregarHijo(consumir(TipoToken.ELSE));
            n.agregarHijo(parsearBloque());
        }
        return n;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  WHILE  →  while ( Cond ) Bloque
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearWhile() {
        Nodo n = new Nodo("WhileSent");
        n.agregarHijo(consumir(TipoToken.WHILE));
        n.agregarHijo(consumir(TipoToken.PARENABRE));
        n.agregarHijo(parsearCondicion());
        n.agregarHijo(consumir(TipoToken.PARENCIERRE));
        n.agregarHijo(parsearBloque());
        return n;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  FOR  →  for ( ForInit ; Cond ; ForUpdate ) Bloque
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearFor() {
        Nodo n = new Nodo("ForSent");
        n.agregarHijo(consumir(TipoToken.FOR));
        n.agregarHijo(consumir(TipoToken.PARENABRE));

        // Init: puede ser declaración (sin consumir ';') o asignación, o vacío
        n.agregarHijo(parsearForInit());
        n.agregarHijo(consumir(TipoToken.PUNTOCOMA));

        // Condición
        if (actual().tipo != TipoToken.PUNTOCOMA) {
            n.agregarHijo(parsearCondicion());
        } else {
            n.agregarHijo(new Nodo("ε", true));
        }
        n.agregarHijo(consumir(TipoToken.PUNTOCOMA));

        // Update: puede ser incremento/decremento o asignación
        if (actual().tipo != TipoToken.PARENCIERRE) {
            n.agregarHijo(parsearForUpdate());
        } else {
            n.agregarHijo(new Nodo("ε", true));
        }
        n.agregarHijo(consumir(TipoToken.PARENCIERRE));
        n.agregarHijo(parsearBloque());
        return n;
    }

    private Nodo parsearForInit() {
        Nodo init = new Nodo("ForInit");
        if (esTipoDato(actual().tipo)) {
            // Declaración sin ';' final — lo consume el for
            init.agregarHijo(consumir(actual().tipo));
            init.agregarHijo(consumir(TipoToken.ID));
            if (actual().tipo == TipoToken.ASIGNACION) {
                init.agregarHijo(consumir(TipoToken.ASIGNACION));
                init.agregarHijo(parsearExpr());
            }
        } else if (actual().tipo == TipoToken.ID) {
            init.agregarHijo(consumir(TipoToken.ID));
            if (actual().tipo == TipoToken.ASIGNACION ||
                actual().tipo == TipoToken.MAS_IGUAL  ||
                actual().tipo == TipoToken.MENOS_IGUAL) {
                init.agregarHijo(consumir(actual().tipo));
                init.agregarHijo(parsearExpr());
            }
        } else {
            init.agregarHijo(new Nodo("ε", true));
        }
        return init;
    }

    private Nodo parsearForUpdate() {
        Nodo upd = new Nodo("ForUpdate");
        if (actual().tipo == TipoToken.ID) {
            upd.agregarHijo(consumir(TipoToken.ID));
            TipoToken sig = actual().tipo;
            if (sig == TipoToken.INCREMENTO || sig == TipoToken.DECREMENTO) {
                upd.agregarHijo(consumir(sig));
            } else if (sig == TipoToken.ASIGNACION ||
                       sig == TipoToken.MAS_IGUAL  ||
                       sig == TipoToken.MENOS_IGUAL) {
                upd.agregarHijo(consumir(sig));
                upd.agregarHijo(parsearExpr());
            }
        }
        return upd;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  RETURN  →  return [ Expr ] ;
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearReturn() {
        Nodo n = new Nodo("ReturnSent");
        n.agregarHijo(consumir(TipoToken.RETURN));
        if (actual().tipo != TipoToken.PUNTOCOMA) {
            n.agregarHijo(parsearExpr());
        }
        n.agregarHijo(consumir(TipoToken.PUNTOCOMA));
        return n;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  PRINT  →  print ( Expr ) ;
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearPrint() {
        Nodo n = new Nodo("PrintSent");
        n.agregarHijo(consumir(TipoToken.PRINT));
        n.agregarHijo(consumir(TipoToken.PARENABRE));
        if (actual().tipo != TipoToken.PARENCIERRE) {
            n.agregarHijo(parsearExpr());
        }
        n.agregarHijo(consumir(TipoToken.PARENCIERRE));
        n.agregarHijo(consumir(TipoToken.PUNTOCOMA));
        return n;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  SENTENCIAS QUE EMPIEZAN CON ID
    //  x = expr;  |  x += expr;  |  x++;  |  x--;  |  x;
    //  También: resultado = (expr) / expr;
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearSentenciaID() {
        Nodo sent = new Nodo("AsignSent");
        sent.agregarHijo(consumir(TipoToken.ID));
        TipoToken sig = actual().tipo;

        if (sig == TipoToken.ASIGNACION ||
            sig == TipoToken.MAS_IGUAL  ||
            sig == TipoToken.MENOS_IGUAL ||
            sig == TipoToken.MULT_IGUAL  ||
            sig == TipoToken.DIV_IGUAL) {
            sent.agregarHijo(consumir(sig));
            sent.agregarHijo(parsearExpr());
            sent.agregarHijo(consumir(TipoToken.PUNTOCOMA));

        } else if (sig == TipoToken.INCREMENTO || sig == TipoToken.DECREMENTO) {
            sent.agregarHijo(consumir(sig));
            sent.agregarHijo(consumir(TipoToken.PUNTOCOMA));

        } else if (sig == TipoToken.PARENABRE) {
            // Llamada a función como sentencia: id(args);
            sent.agregarHijo(parsearArgList());
            sent.agregarHijo(consumir(TipoToken.PUNTOCOMA));

        } else {
            sent.agregarHijo(consumir(TipoToken.PUNTOCOMA));
        }
        return sent;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  BLOQUE  →  { ListaSent }
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearBloque() {
        Nodo bloque = new Nodo("Bloque");
        bloque.agregarHijo(consumir(TipoToken.LLAVEABRE));
        bloque.agregarHijo(parsearListaSent());
        bloque.agregarHijo(consumir(TipoToken.LLAVECIERRE));
        return bloque;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  CONDICIÓN  →  Expr { OpRelLog Expr }*
    //  Soporta  a > b && c < d  etc. (cualquier cantidad de operandos)
    // ════════════════════════════════════════════════════════════════ //

    private Nodo parsearCondicion() {
        Nodo cond = new Nodo("Condicion");
        cond.agregarHijo(parsearExpr());
        while (esOperadorRelacional(actual().tipo) || esOperadorLogico(actual().tipo)) {
            cond.agregarHijo(consumir(actual().tipo));
            cond.agregarHijo(parsearExpr());
        }
        return cond;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  EXPRESIONES  (precedencia: suma/resta < mult/div < unario)
    // ════════════════════════════════════════════════════════════════ //

    /** Expr → Termino { (+|-) Termino }* */
    private Nodo parsearExpr() {
        Nodo expr = new Nodo("Expr");
        expr.agregarHijo(parsearTermino());
        while (actual().tipo == TipoToken.MAS || actual().tipo == TipoToken.MENOS) {
            expr.agregarHijo(consumir(actual().tipo));
            expr.agregarHijo(parsearTermino());
        }
        return expr;
    }

    /** Termino → Factor { (*|/|%) Factor }* */
    private Nodo parsearTermino() {
        Nodo term = new Nodo("Termino");
        term.agregarHijo(parsearFactor());
        while (actual().tipo == TipoToken.MULT  ||
               actual().tipo == TipoToken.DIV   ||
               actual().tipo == TipoToken.MODULO) {
            term.agregarHijo(consumir(actual().tipo));
            term.agregarHijo(parsearFactor());
        }
        return term;
    }

    /**
     * Factor → ( Expr )
     *         | - Factor
     *         | ! Factor
     *         | ID [ ++ | -- | ( ArgList ) ]
     *         | Literal
     */
    private Nodo parsearFactor() {
        Nodo  factor = new Nodo("Factor");
        Token t      = actual();

        if (t.tipo == TipoToken.PARENABRE) {
            factor.agregarHijo(consumir(TipoToken.PARENABRE));
            factor.agregarHijo(parsearExpr());
            factor.agregarHijo(consumir(TipoToken.PARENCIERRE));

        } else if (t.tipo == TipoToken.MENOS) {
            factor.agregarHijo(consumir(TipoToken.MENOS));
            factor.agregarHijo(parsearFactor());

        } else if (t.tipo == TipoToken.NOT) {
            factor.agregarHijo(consumir(TipoToken.NOT));
            factor.agregarHijo(parsearFactor());

        } else if (t.tipo == TipoToken.ID) {
            factor.agregarHijo(consumir(TipoToken.ID));
            // postfijo o llamada
            if (actual().tipo == TipoToken.INCREMENTO || actual().tipo == TipoToken.DECREMENTO) {
                factor.agregarHijo(consumir(actual().tipo));
            } else if (actual().tipo == TipoToken.PARENABRE) {
                factor.agregarHijo(parsearArgList());
            }

        } else if (esLiteral(t.tipo)) {
            factor.agregarHijo(consumir(t.tipo));

        } else {
            throw new RuntimeException(
                "Error sintáctico en línea " + t.linea +
                ": Se esperaba una expresión pero se encontró [" +
                t.tipo + " → \"" + t.valor + "\"]"
            );
        }
        return factor;
    }

    /** ArgList → ( [Expr {, Expr}*] ) */
    private Nodo parsearArgList() {
        Nodo args = new Nodo("ArgList");
        args.agregarHijo(consumir(TipoToken.PARENABRE));
        if (actual().tipo != TipoToken.PARENCIERRE) {
            args.agregarHijo(parsearExpr());
            while (actual().tipo == TipoToken.COMA) {
                args.agregarHijo(consumir(TipoToken.COMA));
                args.agregarHijo(parsearExpr());
            }
        }
        args.agregarHijo(consumir(TipoToken.PARENCIERRE));
        return args;
    }

    // ════════════════════════════════════════════════════════════════ //
    //  HELPERS
    // ════════════════════════════════════════════════════════════════ //

    private Nodo consumir(TipoToken esperado) {
        Token t = actual();
        if (t.tipo != esperado) {
            throw new RuntimeException(
                "Error sintáctico en línea " + t.linea +
                ": Se esperaba [" + esperado +
                "] pero se encontró [" + t.tipo + " → \"" + t.valor + "\"]"
            );
        }
        pos++;
        saltarNoSignificativos();
        return new Nodo("\"" + t.valor + "\"", true);
    }

    private Token actual() {
        return tokens.get(pos);
    }

    /** Avanza sobre COMENTARIO_LINEA, COMENTARIO_BLOQUE y DIRECTIVA. */
    private void saltarNoSignificativos() {
        while (pos < tokens.size()) {
            TipoToken tipo = tokens.get(pos).tipo;
            if (tipo == TipoToken.COMENTARIO_LINEA  ||
                tipo == TipoToken.COMENTARIO_BLOQUE ||
                tipo == TipoToken.DIRECTIVA) {
                pos++;
            } else {
                break;
            }
        }
    }

    private boolean puedeIniciarSentencia(TipoToken tipo) {
        return esTipoDato(tipo)           ||
               tipo == TipoToken.IF       ||
               tipo == TipoToken.WHILE    ||
               tipo == TipoToken.FOR      ||
               tipo == TipoToken.RETURN   ||
               tipo == TipoToken.PRINT    ||
               tipo == TipoToken.ID;
    }

    private boolean esTipoDato(TipoToken tipo) {
        return switch (tipo) {
            case INT, DOUBLE, FLOAT, LONG, BOOLEAN, CHAR, STRING,
                 T_INTEGER, T_DOUBLE, T_FLOAT, T_LONG, T_BOOLEAN, T_CHAR -> true;
            default -> false;
        };
    }

    private boolean esOperadorRelacional(TipoToken tipo) {
        return switch (tipo) {
            case IGUALIGUAL, DISTINTO, MAYORQUE, MAYOR_IGUAL,
                 MENORQUE, MENOR_IGUAL -> true;
            default -> false;
        };
    }

    private boolean esOperadorLogico(TipoToken tipo) {
        return tipo == TipoToken.AND || tipo == TipoToken.OR;
    }

    private boolean esLiteral(TipoToken tipo) {
        return switch (tipo) {
            case NUMERO, LITERAL_BOOL, LITERAL_NULO,
                 LITERAL_CADENA, LITERAL_CHAR -> true;
            default -> false;
        };
    }
}