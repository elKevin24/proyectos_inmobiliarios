package com.inmobiliaria.terrenos.application.dto.plano;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CvEngineResponse {
    private String archivo_procesado;
    private int total_lotes_detectados;
    private double tiempo_procesamiento_ms;
    private List<CvLote> lotes;

    @Data
    public static class CvLote {
        private String id_temporal;
        private String numero_lote_detectado;
        private String area_detectada;
        private double confianza_ocr;              // 0-100: Frontend marca en rojo si < 60%
        private List<Map<String, Integer>> poligono;
        private Map<String, Integer> coordenadas_centro;
    }
}
