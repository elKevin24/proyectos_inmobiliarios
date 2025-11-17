package com.inmobiliaria.terrenos.application.dto.fase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para fases
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaseResponse {

    private Long id;
    private Long tenantId;
    private Long proyectoId;
    private String proyectoNombre;
    private String nombre;
    private String descripcion;
    private Integer numeroFase;
    private Integer totalTerrenos;
    private Integer terrenosDisponibles;
    private Integer terrenosApartados;
    private Integer terrenosVendidos;
    private BigDecimal areaTotal;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private String observaciones;
    private Boolean activa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Calcula el porcentaje de avance de la fase (terrenos vendidos)
     */
    public Double getPorcentajeAvance() {
        if (totalTerrenos == null || totalTerrenos == 0) {
            return 0.0;
        }
        int vendidos = terrenosVendidos != null ? terrenosVendidos : 0;
        return (vendidos * 100.0) / totalTerrenos;
    }

    /**
     * Calcula el porcentaje de ocupación (apartados + vendidos)
     */
    public Double getPorcentajeOcupacion() {
        if (totalTerrenos == null || totalTerrenos == 0) {
            return 0.0;
        }
        int apartados = terrenosApartados != null ? terrenosApartados : 0;
        int vendidos = terrenosVendidos != null ? terrenosVendidos : 0;
        return ((apartados + vendidos) * 100.0) / totalTerrenos;
    }

    /**
     * Verifica si la fase está activa
     */
    public boolean isActiva() {
        return Boolean.TRUE.equals(activa);
    }
}
