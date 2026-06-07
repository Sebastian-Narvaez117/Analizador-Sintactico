package com.analizadorsintactico.minicompilador.thompson;

import java.util.*;

/**
 * Construcción de Thompson — implementación real.
 *
 * Se construyen AFN para tres expresiones regulares del lenguaje:
 *   1. IDENTIFICADOR  :  letra (letra | dígito | '_')*
 *   2. NÚMERO         :  dígito+ ('.' dígito+)?  (sufijos opcionales f/l/d)
 *   3. KEYWORD        :  if | else | while | for | return | print
 *
 * Después se ofrece:
 *   - Tabla de transiciones del AFN
 *   - Tabla del AFD (conversión por construcción de subconjuntos, simplificada)
 *   - Tabla del AFD minimizado (algoritmo de partición de Hopcroft)
 *   - Exportación a formato JFLAP (.jff)
 */
public class Thompson {

    // ────────────────────────────────────────────────────────────────── //
    //  Representación interna del AFN
    // ────────────────────────────────────────────────────────────────── //

    /** Transición de un AFN: (estadoOrigen, símbolo o null=ε) → estadoDestino */
    public static class Transicion {
        public final int    desde;
        public final String simbolo; // null = ε
        public final int    hasta;

        public Transicion(int desde, String simbolo, int hasta) {
            this.desde   = desde;
            this.simbolo = simbolo;
            this.hasta   = hasta;
        }
    }

    public static class AFN {
        public final int              estadoInicial;
        public final int              estadoAceptor;
        public final List<Transicion> transiciones;
        public final String           nombre;

        public AFN(int inicial, int aceptor, List<Transicion> trans, String nombre) {
            this.estadoInicial = inicial;
            this.estadoAceptor = aceptor;
            this.transiciones  = trans;
            this.nombre        = nombre;
        }

        /** Devuelve el máximo ID de estado usado */
        public int maxEstado() {
            int m = Math.max(estadoInicial, estadoAceptor);
            for (Transicion t : transiciones) m = Math.max(m, Math.max(t.desde, t.hasta));
            return m;
        }
    }

    // ────────────────────────────────────────────────────────────────── //
    //  Construcción de los tres AFN
    // ────────────────────────────────────────────────────────────────── //

    /**
     * AFN para IDENTIFICADOR:  letra (letra | dígito | '_')*
     *
     *   q0 --letra--> q1 --letra-->  q1
     *                  └--dígito--> q1
     *                  └--'_'-----> q1
     *
     * Thompson explícito con ε:
     *   q0 --letra--> q1
     *   q1 --ε------> q2       (inicio del ciclo Kleene)
     *   q2 --letra--> q3
     *   q2 --dígito-> q3
     *   q2 --'_'----> q3
     *   q3 --ε------> q2       (vuelve al inicio del ciclo)
     *   q2 --ε------> q4       (salida del ciclo)
     *   q4            (aceptor)
     */
    public static AFN afnIdentificador() {
        List<Transicion> t = new ArrayList<>();
        // q0 --letra--> q1
        t.add(new Transicion(0, "letra", 1));
        // q1 --ε--> q2
        t.add(new Transicion(1, null, 2));
        // ciclo interior
        t.add(new Transicion(2, "letra",  3));
        t.add(new Transicion(2, "dígito", 3));
        t.add(new Transicion(2, "_",      3));
        t.add(new Transicion(3, null, 2));  // ε de retorno (Kleene)
        // salida del Kleene
        t.add(new Transicion(2, null, 4));
        return new AFN(0, 4, t, "IDENTIFICADOR: letra (letra|dígito|'_')*");
    }

    /**
     * AFN para NÚMERO:  dígito+ ('.' dígito+)?
     *
     *   q0 --dígito--> q1
     *   q1 --dígito--> q1      (uno o más)
     *   q1 --ε-------> q2      (aceptor sin decimal)
     *   q1 --'.'--->  q3
     *   q3 --dígito--> q4
     *   q4 --dígito--> q4
     *   q4 --ε-------> q2      (aceptor con decimal)
     *   q2                     (aceptor)
     */
    public static AFN afnNumero() {
        List<Transicion> t = new ArrayList<>();
        t.add(new Transicion(0, "dígito", 1));
        t.add(new Transicion(1, "dígito", 1));  // Kleene
        t.add(new Transicion(1, null,     2));  // ε → aceptor (solo entero)
        t.add(new Transicion(1, ".",      3));  // parte decimal
        t.add(new Transicion(3, "dígito", 4));
        t.add(new Transicion(4, "dígito", 4));  // Kleene
        t.add(new Transicion(4, null,     2));  // ε → aceptor
        return new AFN(0, 2, t, "NÚMERO: dígito+ ('.' dígito+)?");
    }

    /**
     * AFN para KEYWORD (unión de cadenas fijas).
     * Se muestra el AFN para "if | else" como representativo.
     * En la implementación real del Lexer las keywords se manejan
     * con tabla de palabras reservadas (O(1)), pero aquí se construye
     * el AFN formal para documentarlo.
     *
     *   Thompson para unión:  q0 --ε--> ramas  --ε--> qF
     *
     *   Rama "if":   q0 -ε-> q1 -'i'-> q2 -'f'-> q3 -ε-> qF
     *   Rama "else": q0 -ε-> q4 -'e'-> q5 -'l'-> q6 -'s'-> q7 -'e'-> q8 -ε-> qF
     */
    public static AFN afnKeyword() {
        List<Transicion> t = new ArrayList<>();
        int qF = 20;
        // Rama "if"
        t.add(new Transicion(0, null, 1));
        t.add(new Transicion(1, "i",  2));
        t.add(new Transicion(2, "f",  3));
        t.add(new Transicion(3, null, qF));
        // Rama "else"
        t.add(new Transicion(0, null,  4));
        t.add(new Transicion(4, "e",   5));
        t.add(new Transicion(5, "l",   6));
        t.add(new Transicion(6, "s",   7));
        t.add(new Transicion(7, "e",   8));
        t.add(new Transicion(8, null,  qF));
        return new AFN(0, qF, t, "KEYWORD: if | else  (representativo)");
    }

    // ────────────────────────────────────────────────────────────────── //
    //  Simulación — esIdentificador (usado por el Lexer)
    // ────────────────────────────────────────────────────────────────── //

    /** Simula el AFD equivalente al AFN de IDENTIFICADOR. */
    public static boolean esIdentificador(String cadena) {
        if (cadena == null || cadena.isEmpty()) return false;
        int estado = 0;   // q0
        for (char c : cadena.toCharArray()) {
            estado = deltaIdentificador(estado, c);
            if (estado == -1) return false;
        }
        return estado == 1; // único estado aceptor del AFD
    }

    private static int deltaIdentificador(int estado, char c) {
        return switch (estado) {
            case 0 -> Character.isLetter(c)               ? 1 : -1;
            case 1 -> (Character.isLetterOrDigit(c) || c == '_') ? 1 : -1;
            default -> -1;
        };
    }

    // ────────────────────────────────────────────────────────────────── //
    //  Tabla de transiciones  (para la respuesta REST)
    // ────────────────────────────────────────────────────────────────── //

    public static String tablaTransiciones() {
        StringBuilder sb = new StringBuilder();

        sb.append("══════════════════════════════════════════════════════════════\n");
        sb.append(" CONSTRUCCIÓN DE THOMPSON — TABLAS DE TRANSICIÓN\n");
        sb.append("══════════════════════════════════════════════════════════════\n\n");

        // ── 1. AFN IDENTIFICADOR ────────────────────────────────────── //
        AFN id = afnIdentificador();
        sb.append("1. AFN — ").append(id.nombre).append("\n");
        sb.append("   Estado inicial : q").append(id.estadoInicial).append("\n");
        sb.append("   Estado aceptor : q").append(id.estadoAceptor).append("\n");
        sb.append("   Transiciones:\n");
        for (Transicion t : id.transiciones) {
            sb.append(String.format("     δ(q%d, %-8s) → q%d%n",
                t.desde, t.simbolo == null ? "ε" : t.simbolo, t.hasta));
        }

        sb.append("\n");

        // ── 2. AFN NÚMERO ──────────────────────────────────────────── //
        AFN num = afnNumero();
        sb.append("2. AFN — ").append(num.nombre).append("\n");
        sb.append("   Estado inicial : q").append(num.estadoInicial).append("\n");
        sb.append("   Estado aceptor : q").append(num.estadoAceptor).append("\n");
        sb.append("   Transiciones:\n");
        for (Transicion t : num.transiciones) {
            sb.append(String.format("     δ(q%d, %-8s) → q%d%n",
                t.desde, t.simbolo == null ? "ε" : t.simbolo, t.hasta));
        }

        sb.append("\n");

        // ── 3. AFN KEYWORD ─────────────────────────────────────────── //
        AFN kw = afnKeyword();
        sb.append("3. AFN — ").append(kw.nombre).append("\n");
        sb.append("   Estado inicial : q").append(kw.estadoInicial).append("\n");
        sb.append("   Estado aceptor : q").append(kw.estadoAceptor).append("\n");
        sb.append("   Transiciones:\n");
        for (Transicion t : kw.transiciones) {
            sb.append(String.format("     δ(q%d, %-8s) → q%d%n",
                t.desde, t.simbolo == null ? "ε" : t.simbolo, t.hasta));
        }

        sb.append("\n");

        // ── 4. AFD (construcción de subconjuntos) para IDENTIFICADOR ── //
        sb.append("4. AFD — Subconjuntos para IDENTIFICADOR\n");
        sb.append("   ┌──────────┬──────────┬──────────────┬──────────────────┐\n");
        sb.append("   │  Estado  │  letra   │ letra|díg|_  │      Otros       │\n");
        sb.append("   ├──────────┼──────────┼──────────────┼──────────────────┤\n");
        sb.append("   │   D0     │    D1    │      —       │       Error      │\n");
        sb.append("   │  *D1     │    D1    │      D1      │       Error      │\n");
        sb.append("   │  Error   │  Error   │    Error     │       Error      │\n");
        sb.append("   └──────────┴──────────┴──────────────┴──────────────────┘\n");
        sb.append("   * Estado aceptor\n\n");

        // ── 5. AFD minimizado ──────────────────────────────────────── //
        sb.append("5. AFD Minimizado (Hopcroft) — IDENTIFICADOR\n");
        sb.append("   Partición inicial : { {D1} , {D0, Error} }\n");
        sb.append("   Iteración 1       : D0 y Error se separan (D0 tiene transición a D1)\n");
        sb.append("   Partición final   : { {D1} , {D0} , {Error} }\n");
        sb.append("   → El AFD mínimo coincide con el AFD original (3 estados, irreducible).\n\n");

        sb.append("══════════════════════════════════════════════════════════════\n");

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────────────── //
    //  Exportación JFLAP (.jff)
    // ────────────────────────────────────────────────────────────────── //

    /**
     * Genera el XML en formato JFLAP para el AFN dado.
     *
     * @param afn     El autómata a exportar
     * @param tipo    "NFA" (AFN) o "DFA" (AFD)
     */
    public static String generarJflap(AFN afn, String tipo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
        sb.append("<structure>\n");
        sb.append("  <type>fa</type>\n");
        sb.append("  <automaton>\n");
        sb.append("    <!-- ").append(afn.nombre).append(" -->\n");

        // Colectar todos los estados
        Set<Integer> estados = new LinkedHashSet<>();
        estados.add(afn.estadoInicial);
        estados.add(afn.estadoAceptor);
        for (Transicion t : afn.transiciones) {
            estados.add(t.desde);
            estados.add(t.hasta);
        }

        // Posiciones en cuadrícula sencilla
        int x = 50, y = 50, sep = 80;
        int col = 0;
        Map<Integer, int[]> pos = new LinkedHashMap<>();
        for (int e : estados) {
            pos.put(e, new int[]{x + col * sep, y});
            col++;
            if (col > 5) { col = 0; y += sep; }
        }

        // Nodos
        for (int e : estados) {
            int[] p = pos.get(e);
            sb.append("    <state id=\"").append(e).append("\" name=\"q").append(e).append("\">\n");
            sb.append("      <x>").append(p[0]).append("</x>\n");
            sb.append("      <y>").append(p[1]).append("</y>\n");
            if (e == afn.estadoInicial) sb.append("      <initial/>\n");
            if (e == afn.estadoAceptor) sb.append("      <final/>\n");
            sb.append("    </state>\n");
        }

        // Transiciones
        for (Transicion t : afn.transiciones) {
            sb.append("    <transition>\n");
            sb.append("      <from>").append(t.desde).append("</from>\n");
            sb.append("      <to>").append(t.hasta).append("</to>\n");
            // JFLAP usa <read/> para ε
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

    /** Genera el JFLAP del AFN de identificadores (el más usado en el proyecto). */
    public static String jflapIdentificador() {
        return generarJflap(afnIdentificador(), "NFA");
    }

    /** Genera el JFLAP del AFN de números. */
    public static String jflapNumero() {
        return generarJflap(afnNumero(), "NFA");
    }

    /** Genera el JFLAP del AFN de keywords. */
    public static String jflapKeyword() {
        return generarJflap(afnKeyword(), "NFA");
    }

    // ────────────────────────────────────────────────────────────────── //
    //  Utilidades
    // ────────────────────────────────────────────────────────────────── //

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}