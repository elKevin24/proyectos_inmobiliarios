package com.inmobiliaria.terrenos.application.dto.terreno;

import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para terrenos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerrenoResponse {

    private Long id;
    private Long tenantId;
    private Long proyectoId;
    private String proyectoNombre;
    private Long faseId;
    private String faseNombre;
    private String numeroLote;
    private String manzana;
    private BigDecimal area;
    private BigDecimal frente;
    private BigDecimal fondo;
    private BigDecimal precioBase;
    private BigDecimal precioAjuste;
    private BigDecimal precioMultiplicador;
    private BigDecimal precioFinal;
    private String caracteristicas;
    private String observaciones;
    private EstadoTerreno estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Calcula el precio por metro cuadrado
     */
    public BigDecimal getPrecioPorMetro() {
        if (precioFinal == null || area == null || area.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return precioFinal.divide(area, 2, RoundingMode.HALF_UP);
    }

    /**
     * Verifica si el terreno está disponible para venta
     */
    public boolean isDisponible() {
        return estado == EstadoTerreno.DISPONIBLE;
    }

    /**
     * Verifica si el terreno está en proceso de venta
     */
    public boolean isEnProceso() {
        return estado == EstadoTerreno.APARTADO || estado == EstadoTerreno.EN_VENTA;
    }

    /**
     * Obtiene el identificador completo del terreno
     */
    public String getIdentificadorCompleto() {
        if (manzana != null && !manzana.isBlank()) {
            return String.format("Mz %s Lt %s", manzana, numeroLote);
        }
        return String.format("Lt %s", numeroLote);
    }
}
