package com.analizadorsintactico.minicompilador.model;

import java.util.List;

/**
 * Objeto que devuelve la API REST con todos los resultados del análisis.
 */
public class ResultadoAnalisis {

    public boolean      balanceado;
    public String       mensajeBalanceo;
    public List<String> tokens;
    public String       arbolDerivacion;
    public String       tablaThompson;
    public String       mensaje;

    // Archivos JFLAP (XML) listos para guardar como .jff
    public String jflapIdentificador;
    public String jflapNumero;
    public String jflapKeyword;

    public ResultadoAnalisis() {}

    // Constructor corto usado por AnalizadorService (sin JFLAP)
    public ResultadoAnalisis(boolean balanceado,
                              String mensajeBalanceo,
                              List<String> tokens,
                              String arbolDerivacion,
                              String tablaThompson,
                              String mensaje) {
        this.balanceado      = balanceado;
        this.mensajeBalanceo = mensajeBalanceo;
        this.tokens          = tokens;
        this.arbolDerivacion = arbolDerivacion;
        this.tablaThompson   = tablaThompson;
        this.mensaje         = mensaje;
    }

    public ResultadoAnalisis(boolean balanceado,
                              String mensajeBalanceo,
                              List<String> tokens,
                              String arbolDerivacion,
                              String tablaThompson,
                              String mensaje,
                              String jflapIdentificador,
                              String jflapNumero,
                              String jflapKeyword) {
        this.balanceado          = balanceado;
        this.mensajeBalanceo     = mensajeBalanceo;
        this.tokens              = tokens;
        this.arbolDerivacion     = arbolDerivacion;
        this.tablaThompson       = tablaThompson;
        this.mensaje             = mensaje;
        this.jflapIdentificador  = jflapIdentificador;
        this.jflapNumero         = jflapNumero;
        this.jflapKeyword        = jflapKeyword;
    }
}