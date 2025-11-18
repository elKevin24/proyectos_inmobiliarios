package com.inmobiliaria.terrenos.application.dto.pago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para tabla de amortización completa
 * Útil para visualizar el plan de pagos completo
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TablaAmortizacionResponse {

    private Long planPagoId;
    private String clienteNombre;
    private String terrenoNumeroLote;

    // Información del plan
    private BigDecimal montoFinanciado;
    private BigDecimal tasaInteresAnual;
    private Integer numeroPagos;
    private String frecuenciaPago;

    // Tabla de amortización
    private List<AmortizacionResponse> amortizaciones;

    // Totales
    private Totales totales;

    /**
     * Totales de la tabla de amortización
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Totales {
        private BigDecimal totalCapital;
        private BigDecimal totalInteres;
        private BigDecimal totalPagos;
        private BigDecimal totalPagado;
        private BigDecimal totalPendiente;
    }
}
