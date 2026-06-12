// src/main/java/com/tesis/dto/PrediccionRequest.java
package com.tesis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud de predicción de demanda")
public class PrediccionRequest {

    @NotNull(message = "El mes es requerido")
    @Min(value = 1, message = "El mes debe ser como mínimo 1")
    @Max(value = 12, message = "El mes debe ser como máximo 12")
    @Schema(description = "Mes del año (1-12) que representa el mes de pedido. El backend calcula el mes objetivo siguiente.", example = "5")
    private Integer mes;

    @NotNull(message = "El año es requerido")
    @Min(value = 2020, message = "El año debe ser como mínimo 2020")
    @Max(value = 2100, message = "El año debe ser como máximo 2100")
    @Schema(description = "Año de la predicción", example = "2026")
    private Integer anio;

    @NotBlank(message = "La categoría es requerida")
    @Schema(description = "Categoría del producto", example = "ROPA BEBE")
    private String categoria;

    public PrediccionRequest() {}

    public PrediccionRequest(Integer mes, Integer anio, String categoria) {
        this.mes = mes;
        this.anio = anio;
        this.categoria = categoria;
    }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
