package com.inmobiliaria.terrenos.application.dto.cliente;

import com.inmobiliaria.terrenos.application.dto.apartado.ApartadoResponse;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CotizacionResponse;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para historial completo de transacciones del cliente
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteHistorialResponse {

    // Información del cliente
    private ClienteResponse cliente;

    // Historial de transacciones
    private List<CotizacionResponse> cotizaciones;
    private List<ApartadoResponse> apartados;
    private List<VentaResponse> ventas;

    // Estadísticas
    private Integer totalCotizaciones;
    private Integer totalApartados;
    private Integer totalVentas;

    // Montos totales
    private BigDecimal montoTotalCotizaciones;
    private BigDecimal montoTotalApartados;
    private BigDecimal montoTotalVentas;

    // Tasa de conversión
    private BigDecimal tasaConversion; // Porcentaje de cotizaciones que terminaron en venta
}
