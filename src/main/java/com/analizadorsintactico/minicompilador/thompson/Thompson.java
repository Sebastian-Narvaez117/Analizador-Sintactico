package com.analizadorsintactico.minicompilador.thompson;

import java.util.*;

public class Thompson {

    public static class Transicion {
        public final int desde;
        public final String simbolo;
        public final int hasta;

        public Transicion(int desde, String simbolo, int hasta) {
            this.desde = desde;
            this.simbolo = simbolo;
            this.hasta = hasta;
        }
    }

    public static class AFN {
        public final int estadoInicial;
        public final int estadoAceptor;
        public final List<Transicion> transiciones;
        public final String nombre;

        public AFN(int inicial, int aceptor, List<Transicion> trans, String nombre) {
            this.estadoInicial = inicial;
            this.estadoAceptor = aceptor;
            this.transiciones = trans;
            this.nombre = nombre;
        }

        public int maxEstado() {
            int m = Math.max(estadoInicial, estadoAceptor);
            for (Transicion t : transiciones) {
                m = Math.max(m, Math.max(t.desde, t.hasta));
            }
            return m;
        }
    }

    public static AFN afnIdentificador() {
        List<Transicion> t = new ArrayList<>();

        t.add(new Transicion(0, "letra", 1));
        t.add(new Transicion(1, null, 2));
        t.add(new Transicion(2, "letra", 3));
        t.add(new Transicion(2, "dígito", 3));
        t.add(new Transicion(2, "_", 3));
        t.add(new Transicion(3, null, 2));
        t.add(new Transicion(2, null, 4));

        return new AFN(0, 4, t, "IDENTIFICADOR: letra (letra|dígito|'_')*");
    }

    public static AFN afnNumero() {
        List<Transicion> t = new ArrayList<>();

        t.add(new Transicion(0, "dígito", 1));
        t.add(new Transicion(1, "dígito", 1));
        t.add(new Transicion(1, null, 2));
        t.add(new Transicion(1, ".", 3));
        t.add(new Transicion(3, "dígito", 4));
        t.add(new Transicion(4, "dígito", 4));
        t.add(new Transicion(4, null, 2));

        return new AFN(0, 2, t, "NÚMERO: dígito+ ('.' dígito+)?");
    }

    public static AFN afnKeyword() {
        List<Transicion> t = new ArrayList<>();
        int qF = 20;

        t.add(new Transicion(0, null, 1));
        t.add(new Transicion(1, "i", 2));
        t.add(new Transicion(2, "f", 3));
        t.add(new Transicion(3, null, qF));

        t.add(new Transicion(0, null, 4));
        t.add(new Transicion(4, "e", 5));
        t.add(new Transicion(5, "l", 6));
        t.add(new Transicion(6, "s", 7));
        t.add(new Transicion(7, "e", 8));
        t.add(new Transicion(8, null, qF));

        t.add(new Transicion(0, null, 9));
        t.add(new Transicion(9, "w", 10));
        t.add(new Transicion(10, "h", 11));
        t.add(new Transicion(11, "i", 12));
        t.add(new Transicion(12, "l", 13));
        t.add(new Transicion(13, "e", 14));
        t.add(new Transicion(14, null, qF));

        t.add(new Transicion(0, null, 15));
        t.add(new Transicion(15, "f", 16));
        t.add(new Transicion(16, "o", 17));
        t.add(new Transicion(17, "r", 18));
        t.add(new Transicion(18, null, qF));

        return new AFN(0, qF, t, "KEYWORD: if | else | while | for");
    }

    public static AFN afnBloqueControl() {
        List<Transicion> t = new ArrayList<>();

        /*
         * Expresión regular solicitada:
         *
         *     { (if | while)* }
         *
         * Este AFN demuestra la Construcción de Thompson para un bloque
         * simplificado formado por cero o más apariciones de IF o WHILE.
         *
         * Importante:
         * El AFN no valida anidamiento arbitrario, porque eso corresponde
         * a la GLC y al parser descendente recursivo.
         */

        int q0 = 0;
        int q1 = 1;
        int q2 = 2;
        int q3 = 3;
        int q4 = 4;
        int q5 = 5;
        int q6 = 6;
        int q7 = 7;
        int q8 = 8;
        int q9 = 9;
        int q10 = 10;
        int q11 = 11;
        int q12 = 12;
        int q13 = 13;
        int q14 = 14;

        t.add(new Transicion(q0, "{", q1));

        t.add(new Transicion(q1, null, q2));

        t.add(new Transicion(q2, null, q11));

        t.add(new Transicion(q2, null, q3));
        t.add(new Transicion(q3, "i", q4));
        t.add(new Transicion(q4, "f", q5));
        t.add(new Transicion(q5, null, q10));

        t.add(new Transicion(q2, null, q6));
        t.add(new Transicion(q6, "w", q7));
        t.add(new Transicion(q7, "h", q8));
        t.add(new Transicion(q8, "i", q9));
        t.add(new Transicion(q9, "l", q12));
        t.add(new Transicion(q12, "e", q13));
        t.add(new Transicion(q13, null, q10));

        t.add(new Transicion(q10, null, q2));

        t.add(new Transicion(q11, "}", q14));

        return new AFN(q0, q14, t, "BLOQUE_CONTROL: { (if|while)* }");
    }

    public static boolean esIdentificador(String cadena) {
        if (cadena == null || cadena.isEmpty()) {
            return false;
        }

        int estado = 0;

        for (char c : cadena.toCharArray()) {
            estado = deltaIdentificador(estado, c);
            if (estado == -1) {
                return false;
            }
        }

        return estado == 1;
    }

    private static int deltaIdentificador(int estado, char c) {
        return switch (estado) {
            case 0 -> Character.isLetter(c) ? 1 : -1;
            case 1 -> (Character.isLetterOrDigit(c) || c == '_') ? 1 : -1;
            default -> -1;
        };
    }

    public static String tablaTransiciones() {
        StringBuilder sb = new StringBuilder();

        sb.append("══════════════════════════════════════════════════════════════\n");
        sb.append(" CONSTRUCCIÓN DE THOMPSON — TABLAS DE TRANSICIÓN\n");
        sb.append("══════════════════════════════════════════════════════════════\n\n");

        agregarAFNATabla(sb, "1", afnIdentificador());
        agregarAFNATabla(sb, "2", afnNumero());
        agregarAFNATabla(sb, "3", afnKeyword());
        agregarAFNATabla(sb, "4", afnBloqueControl());

        sb.append("5. AFD — Subconjuntos para IDENTIFICADOR\n");
        sb.append("   ┌──────────┬──────────┬──────────────┬──────────────────┐\n");
        sb.append("   │  Estado  │  letra   │ letra|díg|_  │      Otros       │\n");
        sb.append("   ├──────────┼──────────┼──────────────┼──────────────────┤\n");
        sb.append("   │   D0     │    D1    │      —       │       Error      │\n");
        sb.append("   │  *D1     │    D1    │      D1      │       Error      │\n");
        sb.append("   │  Error   │  Error   │    Error     │       Error      │\n");
        sb.append("   └──────────┴──────────┴──────────────┴──────────────────┘\n");
        sb.append("   * Estado aceptor\n\n");

        sb.append("6. AFD Minimizado — IDENTIFICADOR\n");
        sb.append("   Partición inicial : { {D1} , {D0, Error} }\n");
        sb.append("   Iteración 1       : D0 y Error se separan porque D0 transita a D1.\n");
        sb.append("   Partición final   : { {D1} , {D0} , {Error} }\n");
        sb.append("   Resultado         : 3 estados. El AFD es irreducible.\n\n");

        sb.append("7. Observación formal\n");
        sb.append("   El AFN BLOQUE_CONTROL demuestra Thompson para la expresión regular:\n");
        sb.append("      { (if | while)* }\n");
        sb.append("   La validación real de anidamiento y balanceo arbitrario se realiza mediante\n");
        sb.append("   la Gramática Libre de Contexto y el parser descendente recursivo.\n\n");

        sb.append("══════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }

    private static void agregarAFNATabla(StringBuilder sb, String numero, AFN afn) {
        sb.append(numero).append(". AFN — ").append(afn.nombre).append("\n");
        sb.append("   Estado inicial : q").append(afn.estadoInicial).append("\n");
        sb.append("   Estado aceptor : q").append(afn.estadoAceptor).append("\n");
        sb.append("   Número de estados : ").append(afn.maxEstado() + 1).append("\n");
        sb.append("   Número de transiciones : ").append(afn.transiciones.size()).append("\n");
        sb.append("   Transiciones:\n");

        for (Transicion t : afn.transiciones) {
            sb.append(String.format(
                    "     δ(q%d, %-8s) → q%d%n",
                    t.desde,
                    t.simbolo == null ? "ε" : t.simbolo,
                    t.hasta
            ));
        }

        sb.append("\n");
    }

    public static String generarJflap(AFN afn, String tipo) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        sb.append("<structure>\n");
        sb.append("  <type>fa</type>\n");
        sb.append("  <automaton>\n");
        sb.append("    <!-- ").append(afn.nombre).append(" -->\n");

        Set<Integer> estados = new LinkedHashSet<>();
        estados.add(afn.estadoInicial);
        estados.add(afn.estadoAceptor);

        for (Transicion t : afn.transiciones) {
            estados.add(t.desde);
            estados.add(t.hasta);
        }

        int x = 50;
        int y = 50;
        int sep = 90;
        int col = 0;

        Map<Integer, int[]> posiciones = new LinkedHashMap<>();

        for (int estado : estados) {
            posiciones.put(estado, new int[]{x + col * sep, y});
            col++;

            if (col > 5) {
                col = 0;
                y += sep;
            }
        }

        for (int estado : estados) {
            int[] p = posiciones.get(estado);

            sb.append("    <state id=\"").append(estado).append("\" name=\"q").append(estado).append("\">\n");
            sb.append("      <x>").append(p[0]).append("</x>\n");
            sb.append("      <y>").append(p[1]).append("</y>\n");

            if (estado == afn.estadoInicial) {
                sb.append("      <initial/>\n");
            }

            if (estado == afn.estadoAceptor) {
                sb.append("      <final/>\n");
            }

            sb.append("    </state>\n");
        }

        for (Transicion t : afn.transiciones) {
            sb.append("    <transition>\n");
            sb.append("      <from>").append(t.desde).append("</from>\n");
            sb.append("      <to>").append(t.hasta).append("</to>\n");

            if (t.simbolo == null) {
                sb.append("      <read/>\n");
            } else {
                sb.append("      <read>").append(escapeXml(t.simbolo)).append("</read>\n");
            }

            sb.append("    </transition>\n");
        }

        sb.append("  </automaton>\n");
        sb.append("</structure>\n");

        return sb.toString();
    }

    public static String jflapIdentificador() {
        return generarJflap(afnIdentificador(), "NFA");
    }

    public static String jflapNumero() {
        return generarJflap(afnNumero(), "NFA");
    }

    public static String jflapKeyword() {
        return generarJflap(afnKeyword(), "NFA");
    }

    public static String jflapBloqueControl() {
        return generarJflap(afnBloqueControl(), "NFA");
    }

    private static String escapeXml(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}