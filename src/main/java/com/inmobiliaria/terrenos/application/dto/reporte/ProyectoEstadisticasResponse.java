package com.inmobiliaria.terrenos.application.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de estadísticas por proyecto
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoEstadisticasResponse {

    private Long proyectoId;
    private String proyectoNombre;
    private Integer totalTerrenos;
    private Integer terrenosDisponibles;
    private Integer terrenosApartados;
    private Integer terrenosVendidos;
    private BigDecimal porcentajeOcupacion;
    private BigDecimal porcentajeDisponibilidad;
    private BigDecimal montoTotalVentas;
    private Long numeroVentas;
    private BigDecimal ticketPromedio;
}
