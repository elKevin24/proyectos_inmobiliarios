package com.inmobiliaria.terrenos.application.dto.cotizacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para cotizaciones
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionResponse {

    private Long id;
    private Long tenantId;
    private Long terrenoId;
    private String terrenoNumeroLote;
    private String terrenoManzana;
    private Long proyectoId;
    private String proyectoNombre;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private BigDecimal precioBase;
    private BigDecimal descuento;
    private BigDecimal porcentajeDescuento;
    private BigDecimal precioFinal;
    private LocalDate fechaVigencia;
    private String observaciones;
    private Boolean vigente;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Verifica si la cotización está vigente
     */
    public boolean isVigente() {
        if (fechaVigencia == null) {
            return false;
        }
        return !fechaVigencia.isBefore(LocalDate.now());
    }

    /**
     * Calcula el ahorro del cliente
     */
    public BigDecimal getAhorro() {
        if (descuento == null) {
            return BigDecimal.ZERO;
        }
        return descuento;
    }

    /**
     * Obtiene identificador del terreno
     */
    public String getTerrenoIdentificador() {
        if (terrenoManzana != null && !terrenoManzana.isBlank()) {
            return String.format("Mz %s Lt %s", terrenoManzana, terrenoNumeroLote);
        }
        return String.format("Lt %s", terrenoNumeroLote);
    }
}
