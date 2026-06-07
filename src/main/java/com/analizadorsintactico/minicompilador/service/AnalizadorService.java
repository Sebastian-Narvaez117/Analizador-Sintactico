package com.analizadorsintactico.minicompilador.service;

import com.analizadorsintactico.minicompilador.lexer.Lexer;
import com.analizadorsintactico.minicompilador.model.Nodo;
import com.analizadorsintactico.minicompilador.model.ResultadoAnalisis;
import com.analizadorsintactico.minicompilador.model.Token;
import com.analizadorsintactico.minicompilador.parser.Parser;
import com.analizadorsintactico.minicompilador.thompson.Thompson;
import com.analizadorsintactico.minicompilador.verificador.VerificadorBalanceo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio principal que orquesta los 3 pasos del análisis:
 *   1. VerificadorBalanceo  — pila de llaves/paréntesis
 *   2. Lexer                — tokenización (usa Thompson internamente)
 *   3. Parser               — árbol de derivación según la GLC
 */
@Service
public class AnalizadorService {

    public ResultadoAnalisis analizar(String codigo) {

        // ── Paso 1: Verificación de balanceo ────────────────────────── //
        VerificadorBalanceo.Resultado balanceo = VerificadorBalanceo.verificar(codigo);

        if (!balanceo.esValido) {
            return new ResultadoAnalisis(
                false,
                balanceo.mensaje,
                null, null,
                Thompson.tablaTransiciones(),
                "Análisis detenido: código no balanceado."
            );
        }

        // ── Paso 2: Análisis léxico ──────────────────────────────────── //
        Lexer        lexer  = new Lexer(codigo);
        List<Token>  tokens = lexer.tokenizar();

        List<String> tokensTexto = tokens.stream()
            .map(Token::toString)
            .collect(Collectors.toList());

        // ── Paso 3: Análisis sintáctico (árbol de derivación) ────────── //
        try {
            Parser parser = new Parser(tokens);
            Nodo   arbol  = parser.parsearPrograma();

            return new ResultadoAnalisis(
                true,
                balanceo.mensaje,
                tokensTexto,
                arbol.aTexto(0),
                Thompson.tablaTransiciones(),
                "Análisis completado correctamente."
            );

        } catch (RuntimeException e) {
            return new ResultadoAnalisis(
                false,
                balanceo.mensaje,
                tokensTexto,
                null,
                Thompson.tablaTransiciones(),
                e.getMessage()
            );
        }
    }
}
