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

@Service
public class AnalizadorService {

    public ResultadoAnalisis analizar(String codigo) {

        VerificadorBalanceo.Resultado balanceo = VerificadorBalanceo.verificar(codigo);

        if (!balanceo.esValido) {
            return new ResultadoAnalisis(
                    false,
                    balanceo.mensaje,
                    null,
                    null,
                    null,
                    Thompson.tablaTransiciones(),
                    "Análisis detenido: el código no está balanceado.",
                    Thompson.jflapIdentificador(),
                    Thompson.jflapNumero(),
                    Thompson.jflapKeyword(),
                    Thompson.jflapBloqueControl()
            );
        }

        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.tokenizar();

        List<String> tokensTexto = tokens.stream()
                .map(Token::toString)
                .collect(Collectors.toList());

        try {
            Parser parser = new Parser(tokens);
            Nodo arbol = parser.parsearPrograma();

            return new ResultadoAnalisis(
                    true,
                    balanceo.mensaje,
                    tokensTexto,
                    arbol.aTexto(0),
                    arbol.aGraphviz(),
                    Thompson.tablaTransiciones(),
                    "Análisis léxico, sintáctico y construcción del árbol completados correctamente.",
                    Thompson.jflapIdentificador(),
                    Thompson.jflapNumero(),
                    Thompson.jflapKeyword(),
                    Thompson.jflapBloqueControl()
            );

        } catch (RuntimeException e) {
            return new ResultadoAnalisis(
                    false,
                    balanceo.mensaje,
                    tokensTexto,
                    null,
                    null,
                    Thompson.tablaTransiciones(),
                    e.getMessage(),
                    Thompson.jflapIdentificador(),
                    Thompson.jflapNumero(),
                    Thompson.jflapKeyword(),
                    Thompson.jflapBloqueControl()
            );
        }
    }
}