// src/main/java/com/tesis/dto/DetalleProductoDTO.java
package com.tesis.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de cantidad sugerida por producto")
public class DetalleProductoDTO {

    @Schema(description = "Nombre del producto", example = "POLO ALGODON KARZY T-2")
    private String producto;

    @Schema(description = "Cantidad sugerida a comprar", example = "12")
    private Integer cantidadSugerida;

    @Schema(description = "Porcentaje histórico de participación en ventas", example = "25.5")
    private Double porcentajeHistorico;

    public DetalleProductoDTO() {}

    public DetalleProductoDTO(String producto, Integer cantidadSugerida, Double porcentajeHistorico) {
        this.producto = producto;
        this.cantidadSugerida = cantidadSugerida;
        this.porcentajeHistorico = porcentajeHistorico;
    }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }

    public Integer getCantidadSugerida() { return cantidadSugerida; }
    public void setCantidadSugerida(Integer cantidadSugerida) { this.cantidadSugerida = cantidadSugerida; }

    public Double getPorcentajeHistorico() { return porcentajeHistorico; }
    public void setPorcentajeHistorico(Double porcentajeHistorico) { this.porcentajeHistorico = porcentajeHistorico; }
}
