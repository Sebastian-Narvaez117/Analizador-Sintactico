package com.analizadorsintactico.minicompilador.model;

import java.util.List;

public class ResultadoAnalisis {

    public boolean balanceado;
    public String mensajeBalanceo;
    public List<String> tokens;
    public String arbolDerivacion;

    // Nuevo: árbol real en formato Graphviz DOT
    public String arbolGraphviz;

    public String tablaThompson;
    public String mensaje;

    public String jflapIdentificador;
    public String jflapNumero;
    public String jflapKeyword;

    // Nuevo: AFN del bloque solicitado por el escenario
    public String jflapBloqueControl;

    public ResultadoAnalisis() {
    }

    public ResultadoAnalisis(
            boolean balanceado,
            String mensajeBalanceo,
            List<String> tokens,
            String arbolDerivacion,
            String arbolGraphviz,
            String tablaThompson,
            String mensaje,
            String jflapIdentificador,
            String jflapNumero,
            String jflapKeyword,
            String jflapBloqueControl
    ) {
        this.balanceado = balanceado;
        this.mensajeBalanceo = mensajeBalanceo;
        this.tokens = tokens;
        this.arbolDerivacion = arbolDerivacion;
        this.arbolGraphviz = arbolGraphviz;
        this.tablaThompson = tablaThompson;
        this.mensaje = mensaje;
        this.jflapIdentificador = jflapIdentificador;
        this.jflapNumero = jflapNumero;
        this.jflapKeyword = jflapKeyword;
        this.jflapBloqueControl = jflapBloqueControl;
    }
}