package com.analizadorsintactico.minicompilador.lexer;

import com.analizadorsintactico.minicompilador.model.TipoToken;
import com.analizadorsintactico.minicompilador.model.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer — convierte el código fuente en una lista de tokens.
 *
 * Reconoce:
 *   Comentarios         : // linea    y   /* bloque *\/
 *   Directivas          : #identificador  (ej. #fin_programa)
 *   Palabras reservadas : if  else  while  for  return  print
 *   Tipos de datos      : int  double  float  long  boolean  char  String
 *   Wrappers            : Integer  Double  Float  Long  Boolean  Char
 *   Literales           : true  false  null  "cadena"  'c'
 *                         número entero/decimal/negativo con sufijos f F l L d D
 *   Identificadores     : letra(letra|dígito|_)*  validado con Thompson
 *   Operadores          : = == != > >= < <= + ++ += - -- -= * *= / /= % && || !
 *   Agrupación          : ( ) { } [ ]
 *   Separadores         : ;  ,  .
 */
public class Lexer {

    private final String fuente;
    private int pos;
    private int linea;

    public Lexer(String fuente) {
        this.fuente = fuente;
        this.pos    = 0;
        this.linea  = 1;
    }

    public List<Token> tokenizar() {
        List<Token> tokens = new ArrayList<>();

        while (pos < fuente.length()) {
            saltarEspacios();
            if (pos >= fuente.length()) break;

            char c = fuente.charAt(pos);

            // ── Comentario de línea // ─────────────────────────────── //
            if (c == '/' && sigueCaracter('/')) {
                tokens.add(leerComentarioLinea());

            // ── Comentario de bloque /* */ ────────────────────────── //
            } else if (c == '/' && sigueCaracter('*')) {
                tokens.add(leerComentarioBloque());

            // ── Directiva #... ────────────────────────────────────── //
            } else if (c == '#') {
                tokens.add(leerDirectiva());

            // ── Identificadores y palabras reservadas ──────────────── //
            } else if (Character.isLetter(c) || c == '_') {
                tokens.add(leerIdentificador());

            // ── Números (incluyendo negativos precedidos por MENOS) ── //
            } else if (Character.isDigit(c)) {
                tokens.add(leerNumero(false));

            // ── Signo menos: puede ser -, --, -=, o número negativo ── //
            } else if (c == '-') {
                // Número negativo: el '-' va pegado a un dígito y el token anterior
                // NO es un valor (ID, número, cierra-paren, cierra-corchete).
                // En cualquier otro caso es operador.
                if (esContextoNegativo(tokens) &&
                    pos + 1 < fuente.length() &&
                    Character.isDigit(fuente.charAt(pos + 1))) {
                    pos++; // consume el '-'
                    Token num = leerNumero(true);
                    tokens.add(num);
                } else if (sigueCaracter('-')) {
                    tokens.add(new Token(TipoToken.DECREMENTO,  "--", linea)); pos += 2;
                } else if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.MENOS_IGUAL, "-=", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.MENOS,       "-",  linea)); pos++;
                }

            // ── Cadenas de texto "..." ────────────────────────────── //
            } else if (c == '"') {
                tokens.add(leerCadena());

            // ── Char literal '.' ──────────────────────────────────── //
            } else if (c == '\'') {
                tokens.add(leerCharLiteral());

            // ── Operador = o == ───────────────────────────────────── //
            } else if (c == '=') {
                if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.IGUALIGUAL, "==", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.ASIGNACION, "=",  linea)); pos++;
                }

            // ── Operador ! o != ───────────────────────────────────── //
            } else if (c == '!') {
                if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.DISTINTO, "!=", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.NOT, "!", linea)); pos++;
                }

            // ── Operador > o >= ───────────────────────────────────── //
            } else if (c == '>') {
                if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.MAYOR_IGUAL, ">=", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.MAYORQUE, ">", linea)); pos++;
                }

            // ── Operador < o <= ───────────────────────────────────── //
            } else if (c == '<') {
                if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.MENOR_IGUAL, "<=", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.MENORQUE, "<", linea)); pos++;
                }

            // ── Operador + o ++ o += ──────────────────────────────── //
            } else if (c == '+') {
                if (sigueCaracter('+')) {
                    tokens.add(new Token(TipoToken.INCREMENTO, "++", linea)); pos += 2;
                } else if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.MAS_IGUAL,  "+=", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.MAS,        "+",  linea)); pos++;
                }

            // ── Operador * o *= ───────────────────────────────────── //
            } else if (c == '*') {
                if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.MULT_IGUAL, "*=", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.MULT,       "*",  linea)); pos++;
                }

            // ── Operador / o /= (bloque ya manejado arriba) ───────── //
            } else if (c == '/') {
                if (sigueCaracter('=')) {
                    tokens.add(new Token(TipoToken.DIV_IGUAL, "/=", linea)); pos += 2;
                } else {
                    tokens.add(new Token(TipoToken.DIV,       "/",  linea)); pos++;
                }

            } else if (c == '%') {
                tokens.add(new Token(TipoToken.MODULO, "%", linea)); pos++;

            // ── Operadores lógicos && y || ────────────────────────── //
            } else if (c == '&') {
                if (sigueCaracter('&')) {
                    tokens.add(new Token(TipoToken.AND, "&&", linea)); pos += 2;
                } else {
                    pos++;
                }
            } else if (c == '|') {
                if (sigueCaracter('|')) {
                    tokens.add(new Token(TipoToken.OR, "||", linea)); pos += 2;
                } else {
                    pos++;
                }

            // ── Agrupación ────────────────────────────────────────── //
            } else if (c == '(') {
                tokens.add(new Token(TipoToken.PARENABRE,      "(", linea)); pos++;
            } else if (c == ')') {
                tokens.add(new Token(TipoToken.PARENCIERRE,    ")", linea)); pos++;
            } else if (c == '{') {
                tokens.add(new Token(TipoToken.LLAVEABRE,      "{", linea)); pos++;
            } else if (c == '}') {
                tokens.add(new Token(TipoToken.LLAVECIERRE,    "}", linea)); pos++;
            } else if (c == '[') {
                tokens.add(new Token(TipoToken.CORCHETEABRE,   "[", linea)); pos++;
            } else if (c == ']') {
                tokens.add(new Token(TipoToken.CORCHETECIERRE, "]", linea)); pos++;

            // ── Separadores ───────────────────────────────────────── //
            } else if (c == ';') {
                tokens.add(new Token(TipoToken.PUNTOCOMA, ";", linea)); pos++;
            } else if (c == ',') {
                tokens.add(new Token(TipoToken.COMA,      ",", linea)); pos++;
            } else if (c == '.') {
                tokens.add(new Token(TipoToken.PUNTO,     ".", linea)); pos++;

            } else {
                System.out.println("Lexer: carácter desconocido '" + c +
                                   "' en línea " + linea + " — ignorado");
                pos++;
            }
        }

        tokens.add(new Token(TipoToken.FIN, "FIN", linea));
        return tokens;
    }

    // ────────────────────────────────────────────────────────────────── //
    // Lectura de comentarios
    // ────────────────────────────────────────────────────────────────── //

    private Token leerComentarioLinea() {
        int lineaInicio = linea;
        int inicio = pos;
        // avanza hasta fin de línea
        while (pos < fuente.length() && fuente.charAt(pos) != '\n') pos++;
        return new Token(TipoToken.COMENTARIO_LINEA,
                         fuente.substring(inicio, pos).trim(), lineaInicio);
    }

    private Token leerComentarioBloque() {
        int lineaInicio = linea;
        int inicio = pos;
        pos += 2; // consume /*
        while (pos + 1 < fuente.length()) {
            if (fuente.charAt(pos) == '\n') linea++;
            if (fuente.charAt(pos) == '*' && fuente.charAt(pos + 1) == '/') {
                pos += 2; // consume */
                break;
            }
            pos++;
        }
        return new Token(TipoToken.COMENTARIO_BLOQUE,
                         fuente.substring(inicio, pos).trim(), lineaInicio);
    }

    // ────────────────────────────────────────────────────────────────── //
    // Directiva  #identificador
    // ────────────────────────────────────────────────────────────────── //

    private Token leerDirectiva() {
        int inicio = pos;
        pos++; // consume '#'
        while (pos < fuente.length() &&
               (Character.isLetterOrDigit(fuente.charAt(pos)) || fuente.charAt(pos) == '_')) {
            pos++;
        }
        return new Token(TipoToken.DIRECTIVA, fuente.substring(inicio, pos), linea);
    }

    // ────────────────────────────────────────────────────────────────── //
    // Identificadores / palabras reservadas
    // ────────────────────────────────────────────────────────────────── //

    private Token leerIdentificador() {
        int inicio = pos;
        while (pos < fuente.length() &&
               (Character.isLetterOrDigit(fuente.charAt(pos)) || fuente.charAt(pos) == '_')) {
            pos++;
        }
        String texto = fuente.substring(inicio, pos);

        return switch (texto) {
            // Control
            case "if"      -> new Token(TipoToken.IF,           texto, linea);
            case "else"    -> new Token(TipoToken.ELSE,         texto, linea);
            case "while"   -> new Token(TipoToken.WHILE,        texto, linea);
            case "for"     -> new Token(TipoToken.FOR,          texto, linea);
            case "return"  -> new Token(TipoToken.RETURN,       texto, linea);
            case "print"   -> new Token(TipoToken.PRINT,        texto, linea);
            // Tipos primitivos
            case "int"     -> new Token(TipoToken.INT,          texto, linea);
            case "double"  -> new Token(TipoToken.DOUBLE,       texto, linea);
            case "float"   -> new Token(TipoToken.FLOAT,        texto, linea);
            case "long"    -> new Token(TipoToken.LONG,         texto, linea);
            case "boolean" -> new Token(TipoToken.BOOLEAN,      texto, linea);
            case "char"    -> new Token(TipoToken.CHAR,         texto, linea);
            // Tipos wrapper / String
            case "String"  -> new Token(TipoToken.STRING,       texto, linea);
            case "Integer" -> new Token(TipoToken.T_INTEGER,    texto, linea);
            case "Double"  -> new Token(TipoToken.T_DOUBLE,     texto, linea);
            case "Float"   -> new Token(TipoToken.T_FLOAT,      texto, linea);
            case "Long"    -> new Token(TipoToken.T_LONG,       texto, linea);
            case "Boolean" -> new Token(TipoToken.T_BOOLEAN,    texto, linea);
            case "Char"    -> new Token(TipoToken.T_CHAR,       texto, linea);
            // Literales
            case "true",
                 "false"   -> new Token(TipoToken.LITERAL_BOOL, texto, linea);
            case "null"    -> new Token(TipoToken.LITERAL_NULO, texto, linea);
            // Identificador genérico (validado con Thompson)
            default        -> new Token(TipoToken.ID,           texto, linea);
        };
    }

    // ────────────────────────────────────────────────────────────────── //
    // Números: enteros, decimales, sufijos f F l L d D, signo negativo
    // ────────────────────────────────────────────────────────────────── //

    private Token leerNumero(boolean negativo) {
        int inicio = pos;
        // parte entera
        while (pos < fuente.length() && Character.isDigit(fuente.charAt(pos))) pos++;
        // parte decimal
        if (pos < fuente.length() && fuente.charAt(pos) == '.' &&
            pos + 1 < fuente.length() && Character.isDigit(fuente.charAt(pos + 1))) {
            pos++; // consume '.'
            while (pos < fuente.length() && Character.isDigit(fuente.charAt(pos))) pos++;
        }
        // sufijo opcional: f F l L d D
        if (pos < fuente.length()) {
            char suf = fuente.charAt(pos);
            if (suf == 'f' || suf == 'F' || suf == 'l' || suf == 'L' ||
                suf == 'd' || suf == 'D') {
                pos++;
            }
        }
        String raw = fuente.substring(inicio, pos);
        String valor = negativo ? "-" + raw : raw;
        return new Token(TipoToken.NUMERO, valor, linea);
    }

    // ────────────────────────────────────────────────────────────────── //
    // Cadenas  "..."
    // ────────────────────────────────────────────────────────────────── //

    private Token leerCadena() {
        int lineaInicio = linea;
        pos++; // salta '"' apertura
        int inicio = pos;
        while (pos < fuente.length() && fuente.charAt(pos) != '"') {
            if (fuente.charAt(pos) == '\\') pos++; // secuencia de escape
            if (pos < fuente.length() && fuente.charAt(pos) == '\n') linea++;
            pos++;
        }
        String contenido = fuente.substring(inicio, pos);
        if (pos < fuente.length()) pos++; // salta '"' cierre
        return new Token(TipoToken.LITERAL_CADENA, "\"" + contenido + "\"", lineaInicio);
    }

    // ────────────────────────────────────────────────────────────────── //
    // Char literal  'c'  o  '\n'
    // ────────────────────────────────────────────────────────────────── //

    private Token leerCharLiteral() {
        int lineaInicio = linea;
        pos++; // salta '\'' apertura
        int inicio = pos;
        if (pos < fuente.length() && fuente.charAt(pos) == '\\') pos++; // escape
        if (pos < fuente.length()) pos++; // el carácter
        String contenido = fuente.substring(inicio, pos);
        if (pos < fuente.length() && fuente.charAt(pos) == '\'') pos++; // salta '\'' cierre
        return new Token(TipoToken.LITERAL_CHAR, "'" + contenido + "'", lineaInicio);
    }

    // ────────────────────────────────────────────────────────────────── //
    // Utilidades
    // ────────────────────────────────────────────────────────────────── //

    private boolean sigueCaracter(char esperado) {
        return (pos + 1 < fuente.length()) && fuente.charAt(pos + 1) == esperado;
    }

    private void saltarEspacios() {
        while (pos < fuente.length()) {
            char c = fuente.charAt(pos);
            if      (c == '\n')                 { linea++; pos++; }
            else if (Character.isWhitespace(c)) { pos++; }
            else break;
        }
    }

    /**
     * Determina si el contexto actual permite interpretar '-' como signo negativo
     * de un número en vez de operador binario.
     * El '-' actúa como signo negativo cuando el token anterior (si existe) es
     * un operador, separador o apertura de agrupación — nunca después de un valor.
     */
    private boolean esContextoNegativo(List<Token> tokens) {
        if (tokens.isEmpty()) return true;
        TipoToken ultimo = tokens.get(tokens.size() - 1).tipo;
        return switch (ultimo) {
            case ASIGNACION, MAS_IGUAL, MENOS_IGUAL, MULT_IGUAL, DIV_IGUAL,
                 MAS, MENOS, MULT, DIV, MODULO,
                 AND, OR, NOT,
                 IGUALIGUAL, DISTINTO, MAYORQUE, MAYOR_IGUAL, MENORQUE, MENOR_IGUAL,
                 PARENABRE, CORCHETEABRE, COMA, PUNTOCOMA,
                 COMENTARIO_LINEA, COMENTARIO_BLOQUE -> true;
            default -> false;
        };
    }
}