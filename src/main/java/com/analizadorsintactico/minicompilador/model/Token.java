package com.analizadorsintactico.minicompilador.model;

/**
 * Representa un símbolo atómico reconocido por el Lexer.
 * Guarda el tipo, el texto original y el número de línea (para errores).
 */
public class Token {

    public final TipoToken tipo;
    public final String    valor;
    public final int       linea;

    public Token(TipoToken tipo, String valor, int linea) {
        this.tipo  = tipo;
        this.valor = valor;
        this.linea = linea;
    }

    @Override
    public String toString() {
        return String.format("Token(%-12s | %-12s | línea %d)", tipo, valor, linea);
    }
}
