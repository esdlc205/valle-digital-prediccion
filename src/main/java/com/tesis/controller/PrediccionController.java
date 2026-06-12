// src/main/java/com/tesis/controller/PrediccionController.java
package com.tesis.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tesis.dto.PrediccionDetalleResponse;
import com.tesis.dto.PrediccionRequest;
import com.tesis.dto.PrediccionResponse;
import com.tesis.model.LogPrediccion;
import com.tesis.service.PrediccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prediccion")
@Validated
@Tag(name = "Predicción", description = "Endpoint de predicción de demanda")
public class PrediccionController {

    private static final Logger log = LoggerFactory.getLogger(PrediccionController.class);

    private final PrediccionService prediccionService;

    public PrediccionController(PrediccionService prediccionService) {
        this.prediccionService = prediccionService;
    }

    @Operation(
        summary = "Predecir demanda de producto",
        description = "Recibe mes, año y categoría. Devuelve demanda predicha, stock de seguridad y recomendación de compra."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Predicción realizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno al ejecutar el modelo")
    })
    @PostMapping
    public ResponseEntity<PrediccionResponse> predecir(@Valid @RequestBody PrediccionRequest request) {
        log.info("POST /api/prediccion - categoría: {}, mes: {}, anio: {}",
                request.getCategoria(), request.getMes(), request.getAnio());
        return ResponseEntity.ok(prediccionService.predecir(request));
    }

    @Operation(
        summary = "Predicción desglosada por producto",
        description = "Devuelve la demanda total de la categoría y la distribuye por producto según el mix histórico de ventas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Predicción detallada realizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno al ejecutar el modelo")
    })
    @PostMapping("/detalle")
    public ResponseEntity<PrediccionDetalleResponse> predecirDetalle(
            @Valid @RequestBody PrediccionRequest request) {
        log.info("POST /api/prediccion/detalle - categoría: {}, mes: {}, anio: {}",
                request.getCategoria(), request.getMes(), request.getAnio());
        return ResponseEntity.ok(prediccionService.predecirDetalle(request));
    }

    @Operation(
        summary = "Obtener historial de predicciones",
        description = "Devuelve todas las predicciones realizadas durante la sesión actual (en memoria)."
    )
    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")
    @GetMapping("/logs")
    public ResponseEntity<List<LogPrediccion>> obtenerLogs() {
        log.info("GET /api/prediccion/logs");
        return ResponseEntity.ok(prediccionService.obtenerHistorial());
    }
}
