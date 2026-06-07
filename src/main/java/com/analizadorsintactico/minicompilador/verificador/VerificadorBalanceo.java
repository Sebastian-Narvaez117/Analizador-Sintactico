package com.analizadorsintactico.minicompilador.verificador;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Paso 1 del análisis: verifica con una pila que todo '(' tiene su ')'
 * y todo '{' tiene su '}', antes de pasar al Lexer y al Parser.
 */
public class VerificadorBalanceo {

    public static Resultado verificar(String codigo) {
        Deque<Character> pila       = new ArrayDeque<>();
        Deque<Integer>   pilaLineas = new ArrayDeque<>();
        int linea = 1;

        for (int i = 0; i < codigo.length(); i++) {
            char c = codigo.charAt(i);

            if (c == '\n') { linea++; continue; }

            if (c == '(' || c == '{') {
                pila.push(c);
                pilaLineas.push(linea);

            } else if (c == ')' || c == '}') {
                if (pila.isEmpty()) {
                    return new Resultado(false,
                        "Cierre '" + c + "' sin apertura en línea " + linea);
                }
                char apertura = pila.pop();
                pilaLineas.pop();
                char esperado = (apertura == '(') ? ')' : '}';
                if (c != esperado) {
                    return new Resultado(false,
                        "Se esperaba '" + esperado + "' pero se encontró '" + c +
                        "' en línea " + linea);
                }
            }
        }

        if (!pila.isEmpty()) {
            int  lineaAp   = pilaLineas.peek();
            char sinCerrar = pila.peek();
            return new Resultado(false,
                "Apertura '" + sinCerrar + "' en línea " + lineaAp + " nunca fue cerrada");
        }

        return new Resultado(true,
            "Balanceo correcto: todos los paréntesis y llaves coinciden");
    }

    // ------------------------------------------------------------------ //
    public static class Resultado {
        public final boolean esValido;
        public final String  mensaje;

        public Resultado(boolean esValido, String mensaje) {
            this.esValido = esValido;
            this.mensaje  = mensaje;
        }
    }
}
