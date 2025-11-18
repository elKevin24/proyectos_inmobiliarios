package com.inmobiliaria.terrenos.application.dto.plano;

import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para visualización de terreno en plano interactivo
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerrenoVisualizacionResponse {

    // Identificación
    private Long id;
    private String numeroLote;
    private String manzana;

    // Estado y color
    private EstadoTerreno estado;
    private String colorHex; // Color según estado: #4CAF50 (verde), #FFC107 (amarillo), #F44336 (rojo)

    // Coordenadas en el plano
    private CoordenadasPlano coordenadas;

    // Datos del terreno
    private BigDecimal area;
    private BigDecimal precioFinal;
    private String caracteristicas;
    private String observaciones;

    // Información adicional
    private Long faseId;
    private String faseNombre;

    /**
     * Obtiene el color según el estado del terreno
     */
    public static String getColorByEstado(EstadoTerreno estado) {
        if (estado == null) {
            return "#9E9E9E"; // Gris para null
        }
        return switch (estado) {
            case DISPONIBLE -> "#4CAF50";  // Verde
            case APARTADO -> "#FFC107";    // Amarillo/Naranja
            case VENDIDO -> "#F44336";     // Rojo
            case RESERVADO -> "#2196F3";   // Azul
        };
    }

    /**
     * Establece el color automáticamente según el estado
     */
    public void setEstadoAndColor(EstadoTerreno estado) {
        this.estado = estado;
        this.colorHex = getColorByEstado(estado);
    }
}
