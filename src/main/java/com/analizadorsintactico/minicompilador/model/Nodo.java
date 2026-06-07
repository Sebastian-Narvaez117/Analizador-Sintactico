package com.analizadorsintactico.minicompilador.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del árbol de derivación.
 * Los nodos internos representan reglas de la GLC.
 * Las hojas representan tokens del código fuente.
 */
public class Nodo {

    public final String     etiqueta;
    public final List<Nodo> hijos;
    public final boolean    esHoja;

    // Nodo interno (regla gramatical)
    public Nodo(String etiqueta) {
        this.etiqueta = etiqueta;
        this.hijos    = new ArrayList<>();
        this.esHoja   = false;
    }

    // Nodo hoja (token)
    public Nodo(String etiqueta, boolean esHoja) {
        this.etiqueta = etiqueta;
        this.hijos    = new ArrayList<>();
        this.esHoja   = esHoja;
    }

    public void agregarHijo(Nodo hijo) {
        if (hijo != null) hijos.add(hijo);
    }

    public void imprimir(String prefijo, boolean esUltimo) {
        String conector = esUltimo ? "└── " : "├── ";
        String tipo     = esHoja   ? "[TOKEN] " : "[NODO]  ";
        System.out.println(prefijo + conector + tipo + etiqueta);

        String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");
        for (int i = 0; i < hijos.size(); i++) {
            hijos.get(i).imprimir(nuevoPrefijo, i == hijos.size() - 1);
        }
    }

    public void imprimirArbol() {
        System.out.println("[NODO]  " + etiqueta);
        for (int i = 0; i < hijos.size(); i++) {
            hijos.get(i).imprimir("", i == hijos.size() - 1);
        }
    }

    /**
     * Convierte el árbol a String con indentación (para la respuesta REST).
     */
    public String aTexto(int profundidad) {
        StringBuilder sb  = new StringBuilder();
        String        ind = "  ".repeat(profundidad);
        String        tipo = esHoja ? "[TOKEN] " : "[NODO]  ";
        sb.append(ind).append(tipo).append(etiqueta).append("\n");
        for (Nodo hijo : hijos) {
            sb.append(hijo.aTexto(profundidad + 1));
        }
        return sb.toString();
    }
}
