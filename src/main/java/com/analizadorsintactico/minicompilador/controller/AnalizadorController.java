package com.analizadorsintactico.minicompilador.controller;

import com.analizadorsintactico.minicompilador.model.ResultadoAnalisis;
import com.analizadorsintactico.minicompilador.service.AnalizadorService;
import org.springframework.web.bind.annotation.*;

/**
 * Recibe el código fuente como texto plano en el body
 * y devuelve un JSON con todos los resultados del análisis.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")   // permite llamadas desde cualquier origen (útil para pruebas)
public class AnalizadorController {

    private final AnalizadorService analizadorService;

    public AnalizadorController(AnalizadorService analizadorService) {
        this.analizadorService = analizadorService;
    }

    @PostMapping(value = "/analizar", consumes = "text/plain")
    public ResultadoAnalisis analizar(@RequestBody String codigo) {
        return analizadorService.analizar(codigo);
    }
}
