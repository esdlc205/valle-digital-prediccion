// src/main/java/com/tesis/model/LogPrediccion.java
package com.tesis.model;

import java.time.LocalDateTime;

public class LogPrediccion {

    private Long id;
    private LocalDateTime fecha;
    private String categoria;
    private Integer mes;
    private Integer anio;
    private Double demandaPredicha;
    private Integer stockSeguridad;
    private Integer recomendacionCompra;

    public LogPrediccion() {}

    public LogPrediccion(Long id, LocalDateTime fecha, String categoria,
                          Integer mes, Integer anio, Double demandaPredicha,
                          Integer stockSeguridad, Integer recomendacionCompra) {
        this.id = id;
        this.fecha = fecha;
        this.categoria = categoria;
        this.mes = mes;
        this.anio = anio;
        this.demandaPredicha = demandaPredicha;
        this.stockSeguridad = stockSeguridad;
        this.recomendacionCompra = recomendacionCompra;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public Double getDemandaPredicha() { return demandaPredicha; }
    public void setDemandaPredicha(Double demandaPredicha) { this.demandaPredicha = demandaPredicha; }

    public Integer getStockSeguridad() { return stockSeguridad; }
    public void setStockSeguridad(Integer stockSeguridad) { this.stockSeguridad = stockSeguridad; }

    public Integer getRecomendacionCompra() { return recomendacionCompra; }
    public void setRecomendacionCompra(Integer recomendacionCompra) { this.recomendacionCompra = recomendacionCompra; }
}
