package com.inmobiliaria.terrenos.application.dto.reporte;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el dashboard principal
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Proyectos
    private Long totalProyectos;
    private Long proyectosActivos;
    private Long proyectosFinalizados;

    // Terrenos
    private Long totalTerrenos;
    private Long terrenosDisponibles;
    private Long terrenosApartados;
    private Long terrenosVendidos;
    private BigDecimal porcentajeOcupacion;

    // Cotizaciones
    private Long totalCotizaciones;
    private Long cotizacionesVigentes;

    // Apartados
    private Long totalApartados;
    private Long apartadosVigentes;
    private Long apartadosVencidos;

    // Ventas
    private Long totalVentas;
    private Long ventasPendientes;
    private Long ventasPagadas;
    private BigDecimal montoTotalVentas;
    private BigDecimal montoTotalComisiones;

    // Métricas
    private BigDecimal ticketPromedio;
    private BigDecimal tasaConversion; // % de cotizaciones que se convierten en ventas
}
