package com.analizadorsintactico.minicompilador.model;

public enum TipoToken {

    // ── Palabras reservadas ──────────────────────────────────────────
    IF, ELSE, WHILE, FOR, RETURN, PRINT,

    // ── Tipos primitivos ─────────────────────────────────────────────
    INT, DOUBLE, FLOAT, LONG, BOOLEAN, CHAR, STRING,

    // ── Tipos wrapper ────────────────────────────────────────────────
    T_INTEGER,   // Integer
    T_DOUBLE,    // Double
    T_FLOAT,     // Float
    T_LONG,      // Long
    T_BOOLEAN,   // Boolean
    T_CHAR,      // Char

    // ── Identificadores y literales ──────────────────────────────────
    ID,
    NUMERO,          // entero o decimal: 42  3.14  -12.5f  100L
    LITERAL_BOOL,    // true | false
    LITERAL_NULO,    // null
    LITERAL_CADENA,  // "texto"
    LITERAL_CHAR,    // 'A'

    // ── Operadores relacionales ──────────────────────────────────────
    IGUALIGUAL,  // ==
    DISTINTO,    // !=
    MAYORQUE,    // >
    MAYOR_IGUAL, // >=
    MENORQUE,    // <
    MENOR_IGUAL, // <=

    // ── Operadores lógicos ───────────────────────────────────────────
    AND,   // &&
    OR,    // ||
    NOT,   // !

    // ── Operadores aritméticos ───────────────────────────────────────
    MAS,    // +
    MENOS,  // -
    MULT,   // *
    DIV,    // /
    MODULO, // %

    // ── Operadores de asignación ─────────────────────────────────────
    ASIGNACION,   // =
    MAS_IGUAL,    // +=
    MENOS_IGUAL,  // -=
    MULT_IGUAL,   // *=
    DIV_IGUAL,    // /=

    // ── Incremento / decremento ──────────────────────────────────────
    INCREMENTO,  // ++
    DECREMENTO,  // --

    // ── Agrupación ───────────────────────────────────────────────────
    PARENABRE,      // (
    PARENCIERRE,    // )
    LLAVEABRE,      // {
    LLAVECIERRE,    // }
    CORCHETEABRE,   // [
    CORCHETECIERRE, // ]

    // ── Separadores ──────────────────────────────────────────────────
    PUNTOCOMA,  // ;
    COMA,       // ,
    PUNTO,      // .

    // ── Comentarios (se incluyen como tokens informativos) ───────────
    COMENTARIO_LINEA,   // // ...
    COMENTARIO_BLOQUE,  // /* ... */

    // ── Directiva especial ───────────────────────────────────────────
    DIRECTIVA,   // #fin_programa  u otras directivas #...

    // ── Fin de entrada ───────────────────────────────────────────────
    FIN
}