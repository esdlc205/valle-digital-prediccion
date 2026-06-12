// src/main/java/com/tesis/dto/PrediccionResponse.java
package com.tesis.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de predicción de demanda con lógica de anticipación")
public class PrediccionResponse {

    @Schema(description = "Categoría del producto", example = "ROPA BEBE")
    private String categoria;

    // ── Mes en que se genera el pedido ────────────────────────────────────────
    @Schema(description = "Nombre del mes en que se genera la recomendación", example = "Mayo")
    private String mesPedido;

    @Schema(description = "Año en que se genera la recomendación", example = "2026")
    private Integer anioPedido;

    // ── Mes objetivo que se desea cubrir ──────────────────────────────────────
    @Schema(description = "Nombre del mes que se desea cubrir con el pedido", example = "Junio")
    private String mesObjetivo;

    @Schema(description = "Año del mes objetivo", example = "2026")
    private Integer anioObjetivo;

    // ── Resultados de la predicción ───────────────────────────────────────────
    @Schema(description = "Demanda predicha para el mes objetivo (unidades enteras)", example = "45")
    private Integer demandaPredicha;

    @Schema(description = "Stock de seguridad recomendado (mínimo 3)", example = "7")
    private Integer stockSeguridad;

    @Schema(description = "Recomendación de compra = demanda + stock", example = "52")
    private Integer recomendacionCompra;

    public PrediccionResponse() {}

    public PrediccionResponse(String categoria,
                               String mesPedido, Integer anioPedido,
                               String mesObjetivo, Integer anioObjetivo,
                               Integer demandaPredicha, Integer stockSeguridad,
                               Integer recomendacionCompra) {
        this.categoria = categoria;
        this.mesPedido = mesPedido;
        this.anioPedido = anioPedido;
        this.mesObjetivo = mesObjetivo;
        this.anioObjetivo = anioObjetivo;
        this.demandaPredicha = demandaPredicha;
        this.stockSeguridad = stockSeguridad;
        this.recomendacionCompra = recomendacionCompra;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getMesPedido() { return mesPedido; }
    public void setMesPedido(String mesPedido) { this.mesPedido = mesPedido; }

    public Integer getAnioPedido() { return anioPedido; }
    public void setAnioPedido(Integer anioPedido) { this.anioPedido = anioPedido; }

    public String getMesObjetivo() { return mesObjetivo; }
    public void setMesObjetivo(String mesObjetivo) { this.mesObjetivo = mesObjetivo; }

    public Integer getAnioObjetivo() { return anioObjetivo; }
    public void setAnioObjetivo(Integer anioObjetivo) { this.anioObjetivo = anioObjetivo; }

    public Integer getDemandaPredicha() { return demandaPredicha; }
    public void setDemandaPredicha(Integer demandaPredicha) { this.demandaPredicha = demandaPredicha; }

    public Integer getStockSeguridad() { return stockSeguridad; }
    public void setStockSeguridad(Integer stockSeguridad) { this.stockSeguridad = stockSeguridad; }

    public Integer getRecomendacionCompra() { return recomendacionCompra; }
    public void setRecomendacionCompra(Integer recomendacionCompra) { this.recomendacionCompra = recomendacionCompra; }
}
