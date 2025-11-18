package com.inmobiliaria.terrenos.application.dto.plano;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para coordenadas de un terreno en el plano
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordenadasPlano {

    private String tipo; // "poligono", "rectangulo", "circulo"
    private List<Punto> puntos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Punto {
        private Double x;
        private Double y;
    }
}
