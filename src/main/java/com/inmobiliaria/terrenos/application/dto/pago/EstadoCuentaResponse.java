package com.inmobiliaria.terrenos.application.dto.pago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para estado de cuenta completo
 * Este es el DTO más importante del módulo de pagos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoCuentaResponse {

    // Información del plan de pago
    private PlanPagoResponse planPago;

    // Cliente
    private Long clienteId;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;

    // Venta asociada
    private Long ventaId;
    private String terrenoNumeroLote;
    private String proyectoNombre;

    // Tabla de amortización
    private List<AmortizacionResponse> amortizaciones;

    // Historial de pagos
    private List<PagoResponse> pagos;

    // Resumen financiero
    private ResumenFinanciero resumen;

    // Próximos pagos
    private List<AmortizacionResponse> proximasAmortizaciones;

    // Pagos vencidos
    private List<AmortizacionResponse> amortizacionesVencidas;

    /**
     * Resumen financiero del estado de cuenta
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenFinanciero {
        // Montos originales
        private BigDecimal montoTotal;
        private BigDecimal enganche;
        private BigDecimal montoFinanciado;

        // Progreso de pagos
        private BigDecimal totalPagado;
        private BigDecimal totalPendiente;
        private BigDecimal porcentajePagado;

        // Distribución de pagos
        private BigDecimal totalPagadoCapital;
        private BigDecimal totalPagadoInteres;
        private BigDecimal totalPagadoMora;

        // Saldo actual
        private BigDecimal saldoCapital;
        private BigDecimal interesesPendientes;
        private BigDecimal moraPendiente;
        private BigDecimal totalAdeudado; // capital + intereses + mora

        // Estadísticas de cuotas
        private Integer totalCuotas;
        private Integer cuotasPagadas;
        private Integer cuotasPendientes;
        private Integer cuotasVencidas;
        private Integer cuotasParcialesPagadas;

        // Próximo vencimiento
        private LocalDate proximoVencimiento;
        private BigDecimal montoproximaCuota;
        private Integer diasParaProximoPago;

        // Atraso
        private Integer diasAtrasoMaximo;
        private BigDecimal moraAcumuladaTotal;

        // Estado general
        private Boolean estaCorriente; // true si no hay pagos vencidos
        private Boolean tienePagosVencidos;
    }

    /**
     * Calcula si el cliente está al corriente en sus pagos
     */
    public boolean isClienteAlCorriente() {
        return resumen != null && resumen.getEstaCorriente();
    }

    /**
     * Obtiene el total de cuotas vencidas
     */
    public int getCuotasVencidasCount() {
        return amortizacionesVencidas != null ? amortizacionesVencidas.size() : 0;
    }

    /**
     * Obtiene el total de pagos realizados
     */
    public int getPagosRealizadosCount() {
        return pagos != null ? pagos.size() : 0;
    }
}
