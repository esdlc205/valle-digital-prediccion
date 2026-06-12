// src/main/java/com/tesis/service/PrediccionService.java
package com.tesis.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesis.dto.DetalleProductoDTO;
import com.tesis.dto.PrediccionDetalleResponse;
import com.tesis.dto.PrediccionRequest;
import com.tesis.dto.PrediccionResponse;
import com.tesis.model.LogPrediccion;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrediccionService {

    private static final Logger log = LoggerFactory.getLogger(PrediccionService.class);

    private static final String[] MESES = {
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private Booster booster;
    private Map<String, Integer> categoriaMapping;
    private List<String> featuresLista;
    private Map<String, List<Map<String, Object>>> mixVentasMap;
    private Map<String, Double> promediosHistoricos;

    private final List<LogPrediccion> historialPredicciones = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();

            try (InputStream is = getClass().getClassLoader().getResourceAsStream("modelo_xgboost.json")) {
                if (is == null) throw new RuntimeException("No se encontró modelo_xgboost.json");
                this.booster = XGBoost.loadModel(is);
            }

            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream("categoria_mapping.json"), StandardCharsets.UTF_8)) {
                this.categoriaMapping = mapper.readValue(reader, new TypeReference<Map<String, Integer>>() {});
            }

            try (InputStreamReader reader = new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream("features_lista.json"), StandardCharsets.UTF_8)) {
                this.featuresLista = mapper.readValue(reader, new TypeReference<List<String>>() {});
            }

            InputStream mixStream = getClass().getClassLoader().getResourceAsStream("mix_ventas.json");
            if (mixStream != null) {
                try (InputStreamReader reader = new InputStreamReader(mixStream, StandardCharsets.UTF_8)) {
                    this.mixVentasMap = mapper.readValue(reader, new TypeReference<Map<String, List<Map<String, Object>>>>() {});
                }
                log.info("Mix de ventas cargado. Claves: {}", mixVentasMap.size());
            } else {
                this.mixVentasMap = new HashMap<>();
                log.warn("No se encontró mix_ventas.json");
            }

            InputStream promediosStream = getClass().getClassLoader().getResourceAsStream("promedios_historicos.json");
            if (promediosStream != null) {
                try (InputStreamReader reader = new InputStreamReader(promediosStream, StandardCharsets.UTF_8)) {
                    this.promediosHistoricos = mapper.readValue(reader, new TypeReference<Map<String, Double>>() {});
                }
                log.info("Promedios históricos cargados. Entradas: {}", promediosHistoricos.size());
            } else {
                this.promediosHistoricos = new HashMap<>();
                log.warn("No se encontró promedios_historicos.json");
            }

            log.info("Modelo XGBoost cargado. Categorías: {}. Features: {}", categoriaMapping.size(), featuresLista.size());

        } catch (Exception e) {
            log.error("Error al inicializar: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo inicializar el servicio", e);
        }
    }

    // ── Calcula la demanda para un mes y categoría dados ──────────────────────
    // IMPORTANTE: el parámetro "mes" aquí es siempre el mesObjetivo (mes a cubrir),
    // nunca el mesPedido. La conversión se hace en predecir() y predecirDetalle().
    private int calcularDemanda(int mes, String categoriaTexto) throws XGBoostError {
        String categoriaUpper = categoriaTexto.toUpperCase().trim();
        Integer categoriaNum = categoriaMapping.get(categoriaUpper);
        if (categoriaNum == null) {
            categoriaNum = categoriaMapping.getOrDefault(categoriaTexto.trim(), 0);
        }

        int trimestre = (mes - 1) / 3 + 1;
        int semestre = (mes <= 6) ? 1 : 2;

        float[] features = new float[featuresLista.size()];
        for (int i = 0; i < featuresLista.size(); i++) {
            switch (featuresLista.get(i)) {
                case "mes_num"                     -> features[i] = (float) mes;
                case "trimestre"                   -> features[i] = (float) trimestre;
                case "semestre"                    -> features[i] = (float) semestre;
                case "es_frio"                     -> features[i] = (mes >= 5 && mes <= 9) ? 1f : 0f;
                case "es_calor"                    -> features[i] = (mes >= 10 || mes <= 4) ? 1f : 0f;
                case "es_temporada_alta_natalidad" -> features[i] = (mes >= 9 || mes <= 2) ? 1f : 0f;
                case "es_vuelta_clases"            -> features[i] = (mes == 2 || mes == 3) ? 1f : 0f;
                case "es_navidad"                  -> features[i] = (mes == 12 || mes == 1) ? 1f : 0f;
                case "es_dia_madre"                -> features[i] = (mes == 5) ? 1f : 0f;
                case "es_fiestas_patrias"          -> features[i] = (mes == 7) ? 1f : 0f;
                case "categoria_num"               -> features[i] = (float) categoriaNum;
                default -> features[i] = 0.0f;
            }
        }

        DMatrix dmatrix = new DMatrix(features, 1, features.length, Float.NaN);
        float[][] prediccion = booster.predict(dmatrix);

        float valorPrediccion = 0.0f;
        if (prediccion != null && prediccion.length > 0 && prediccion[0].length > 0) {
            valorPrediccion = prediccion[0][0];
        }

        int demanda;
        if (valorPrediccion > 0f) {
            demanda = (valorPrediccion < 1f) ? 1 : Math.max(0, (int) Math.round(valorPrediccion));
        } else {
            demanda = 0;
        }

        if (demanda == 0) {
            String clavePromedio = mes + "_" + categoriaUpper;
            Double promedio = promediosHistoricos.get(clavePromedio);
            if (promedio != null && promedio > 0) {
                demanda = Math.max(1, (int) Math.round(promedio));
                log.info("Respaldo por promedio histórico para {} mes {}: demanda={}", categoriaUpper, mes, demanda);
            } else {
                if (valorPrediccion > 0f && valorPrediccion < 1f) {
                    demanda = 1;
                    log.info("Predicción fraccionaria sin histórico para {} mes {}: demanda mínima=1", categoriaUpper, mes);
                } else {
                    demanda = 0;
                    log.warn("Sin datos históricos para {} mes {}: demanda=0", categoriaUpper, mes);
                }
            }
        }

        return demanda;
    }

    // ── Endpoint principal: predicción total por categoría ────────────────────
    public PrediccionResponse predecir(PrediccionRequest request) {
        try {
            // Mes en que se genera la recomendación (mes del pedido)
            int mesPedido  = request.getMes();
            int anioPedido = request.getAnio();

            // Mes que se desea cubrir con el inventario (mes siguiente)
            int mesObjetivo  = (mesPedido % 12) + 1;
            int anioObjetivo = (mesPedido == 12) ? anioPedido + 1 : anioPedido;

            String categoriaTexto    = request.getCategoria();
            String nombreMesPedido   = MESES[mesPedido - 1];
            String nombreMesObjetivo = MESES[mesObjetivo - 1];

            // La predicción usa mesObjetivo, no mesPedido
            int demandaPredicha     = calcularDemanda(mesObjetivo, categoriaTexto);
            int stockSeguridad      = Math.max(3, (int) Math.round(demandaPredicha * 0.15));
            int recomendacionCompra = demandaPredicha + stockSeguridad;

            log.info("Predicción - pedido en {}/{}, cubre {}/{}, cat={}, demanda={}, stock={}, rec={}",
                    nombreMesPedido, anioPedido, nombreMesObjetivo, anioObjetivo,
                    categoriaTexto.toUpperCase(), demandaPredicha, stockSeguridad, recomendacionCompra);

            historialPredicciones.add(new LogPrediccion(
                    (long) (historialPredicciones.size() + 1),
                    LocalDateTime.now(),
                    categoriaTexto,
                    mesObjetivo,   // se registra el mes que se cubre
                    anioObjetivo,
                    (double) demandaPredicha,
                    stockSeguridad,
                    recomendacionCompra));

            return new PrediccionResponse(
                    categoriaTexto,
                    nombreMesPedido,  anioPedido,
                    nombreMesObjetivo, anioObjetivo,
                    demandaPredicha, stockSeguridad, recomendacionCompra);

        } catch (XGBoostError e) {
            throw new RuntimeException("Error al ejecutar el modelo XGBoost", e);
        }
    }

    // ── Endpoint detalle: predicción desglosada por producto ──────────────────
    public PrediccionDetalleResponse predecirDetalle(PrediccionRequest request) {
        try {
            // Mes en que se genera la recomendación (mes del pedido)
            int mesPedido  = request.getMes();
            int anioPedido = request.getAnio();

            String categoriaTexto    = request.getCategoria();
            String categoriaUpper    = categoriaTexto.toUpperCase().trim();
            String nombreMesPedido   = MESES[mesPedido - 1];

            // Mes que se desea cubrir con el inventario (mes siguiente)
            int mesObjetivo  = (mesPedido % 12) + 1;
            int anioObjetivo = (mesPedido == 12) ? anioPedido + 1 : anioPedido;

            String nombreMesObjetivo = MESES[mesObjetivo - 1];

            // La predicción y el mix usan mesObjetivo, no mesPedido
            int demandaTotal      = calcularDemanda(mesObjetivo, categoriaTexto);
            int stockSeguridad    = Math.max(3, (int) Math.round(demandaTotal * 0.15));
            int recomendacionTotal = demandaTotal + stockSeguridad;

            // Buscar mix de ventas del mes objetivo (no del mes de pedido)
            String clave = mesObjetivo + "_" + categoriaUpper;
            List<Map<String, Object>> mixCategoria = mixVentasMap.get(clave);

            List<DetalleProductoDTO> detalle = new ArrayList<>();

            if (mixCategoria != null && !mixCategoria.isEmpty()) {
                int n = mixCategoria.size();
                int[] cantidades    = new int[n];
                double[] decimales  = new double[n];
                String[] productos  = new String[n];
                double[] porcentajes = new double[n];
                int sumaEnteros = 0;

                // 1. Calcular partes enteras y guardar decimales
                for (int i = 0; i < n; i++) {
                    Map<String, Object> item = mixCategoria.get(i);
                    productos[i]   = (String) item.get("producto");
                    porcentajes[i] = ((Number) item.get("porcentaje")).doubleValue();

                    double cantidadExacta = recomendacionTotal * porcentajes[i] / 100.0;
                    cantidades[i] = (int) Math.floor(cantidadExacta);
                    decimales[i]  = cantidadExacta - cantidades[i];
                    sumaEnteros  += cantidades[i];
                }

                // 2. Método del Mayor Resto para distribuir unidades faltantes
                int faltantes = recomendacionTotal - sumaEnteros;
                while (faltantes > 0) {
                    int maxIdx = 0;
                    for (int i = 1; i < n; i++) {
                        if (decimales[i] > decimales[maxIdx]) maxIdx = i;
                    }
                    cantidades[maxIdx]++;
                    decimales[maxIdx] = -1.0;
                    faltantes--;
                }

                // 3. Llenar lista final (excluir los que quedaron en 0)
                for (int i = 0; i < n; i++) {
                    if (cantidades[i] > 0) {
                        detalle.add(new DetalleProductoDTO(productos[i], cantidades[i], porcentajes[i]));
                    }
                }
            } else {
                log.warn("No se encontró mix de ventas para clave: {}", clave);
            }

            log.info("Detalle - pedido en {}/{}, cubre {}/{}, cat={}, rec={}, productos={}, distribuidos={}",
                    nombreMesPedido, anioPedido, nombreMesObjetivo, anioObjetivo,
                    categoriaUpper, recomendacionTotal, detalle.size(),
                    detalle.stream().mapToInt(DetalleProductoDTO::getCantidadSugerida).sum());

            return new PrediccionDetalleResponse(
                    categoriaTexto,
                    nombreMesPedido,  anioPedido,
                    nombreMesObjetivo, anioObjetivo,
                    demandaTotal, stockSeguridad, detalle);

        } catch (XGBoostError e) {
            throw new RuntimeException("Error al ejecutar el modelo XGBoost", e);
        }
    }

    public List<LogPrediccion> obtenerHistorial() {
        return new ArrayList<>(historialPredicciones);
    }
}
