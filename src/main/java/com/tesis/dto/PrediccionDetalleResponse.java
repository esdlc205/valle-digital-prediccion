// src/main/java/com/tesis/dto/PrediccionDetalleResponse.java
package com.tesis.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de predicción desglosada por producto con lógica de anticipación")
public class PrediccionDetalleResponse {

    @Schema(description = "Categoría del producto", example = "ROPA NIÑO")
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
    @Schema(description = "Demanda total predicha para el mes objetivo", example = "45")
    private Integer demandaTotalCategoria;

    @Schema(description = "Stock de seguridad total (15% de la demanda, mínimo 3)", example = "7")
    private Integer stockSeguridadTotal;

    @Schema(description = "Lista de productos con cantidades sugeridas según mix histórico del mes objetivo")
    private List<DetalleProductoDTO> detalleProductos;

    public PrediccionDetalleResponse() {}

    public PrediccionDetalleResponse(String categoria,
                                      String mesPedido, Integer anioPedido,
                                      String mesObjetivo, Integer anioObjetivo,
                                      Integer demandaTotalCategoria, Integer stockSeguridadTotal,
                                      List<DetalleProductoDTO> detalleProductos) {
        this.categoria = categoria;
        this.mesPedido = mesPedido;
        this.anioPedido = anioPedido;
        this.mesObjetivo = mesObjetivo;
        this.anioObjetivo = anioObjetivo;
        this.demandaTotalCategoria = demandaTotalCategoria;
        this.stockSeguridadTotal = stockSeguridadTotal;
        this.detalleProductos = detalleProductos;
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

    public Integer getDemandaTotalCategoria() { return demandaTotalCategoria; }
    public void setDemandaTotalCategoria(Integer demandaTotalCategoria) { this.demandaTotalCategoria = demandaTotalCategoria; }

    public Integer getStockSeguridadTotal() { return stockSeguridadTotal; }
    public void setStockSeguridadTotal(Integer stockSeguridadTotal) { this.stockSeguridadTotal = stockSeguridadTotal; }

    public List<DetalleProductoDTO> getDetalleProductos() { return detalleProductos; }
    public void setDetalleProductos(List<DetalleProductoDTO> detalleProductos) { this.detalleProductos = detalleProductos; }
}
