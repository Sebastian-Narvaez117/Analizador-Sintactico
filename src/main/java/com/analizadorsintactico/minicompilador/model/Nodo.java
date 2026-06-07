package com.analizadorsintactico.minicompilador.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Nodo {

    public final String etiqueta;
    public final List<Nodo> hijos;
    public final boolean esHoja;

    public Nodo(String etiqueta) {
        this.etiqueta = etiqueta;
        this.hijos = new ArrayList<>();
        this.esHoja = false;
    }

    public Nodo(String etiqueta, boolean esHoja) {
        this.etiqueta = etiqueta;
        this.hijos = new ArrayList<>();
        this.esHoja = esHoja;
    }

    public void agregarHijo(Nodo hijo) {
        if (hijo != null) {
            hijos.add(hijo);
        }
    }

    public String aTexto(int profundidad) {
        StringBuilder sb = new StringBuilder();
        String ind = "  ".repeat(profundidad);
        String tipo = esHoja ? "[TOKEN] " : "[NODO]  ";

        sb.append(ind).append(tipo).append(etiqueta).append("\n");

        for (Nodo hijo : hijos) {
            sb.append(hijo.aTexto(profundidad + 1));
        }

        return sb.toString();
    }

    public String aGraphviz() {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph ArbolDerivacion {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  node [shape=box, style=rounded, fontname=\"Arial\"];\n");

        AtomicInteger contador = new AtomicInteger(0);
        generarDot(this, sb, contador);

        sb.append("}\n");
        return sb.toString();
    }

    private int generarDot(Nodo nodo, StringBuilder sb, AtomicInteger contador) {
        int idActual = contador.getAndIncrement();

        String color = nodo.esHoja ? "#E8F5E9" : "#E3F2FD";
        String etiquetaSegura = escaparDot(nodo.etiqueta);

        sb.append("  n").append(idActual)
          .append(" [label=\"").append(etiquetaSegura)
          .append("\", fillcolor=\"").append(color)
          .append("\", style=\"filled,rounded\"];\n");

        for (Nodo hijo : nodo.hijos) {
            int idHijo = generarDot(hijo, sb, contador);
            sb.append("  n").append(idActual)
              .append(" -> n").append(idHijo)
              .append(";\n");
        }

        return idActual;
    }

    private String escaparDot(String texto) {
        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}