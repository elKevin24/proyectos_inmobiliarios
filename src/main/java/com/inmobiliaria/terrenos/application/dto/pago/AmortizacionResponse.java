package com.inmobiliaria.terrenos.application.dto.pago;

import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para amortización (cuota)
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmortizacionResponse {

    private Long id;
    private Long planPagoId;
    private Integer numeroCuota;

    // Montos de la cuota
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal montoCuota;

    // Montos pagados
    private BigDecimal montoPagado;
    private BigDecimal montoPendiente;

    // Mora
    private BigDecimal moraAcumulada;
    private Integer diasAtraso;

    // Fechas
    private LocalDate fechaVencimiento;
    private LocalDate fechaPago;

    // Estado
    private EstadoAmortizacion estado;
    private String estadoDescripcion;

    // Saldo después del pago
    private BigDecimal saldoRestante;

    // Observaciones
    private String notas;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Indica si la cuota está vencida
     */
    public boolean isVencida() {
        return estado == EstadoAmortizacion.VENCIDO;
    }

    /**
     * Indica si la cuota está completamente pagada
     */
    public boolean isPagada() {
        return estado == EstadoAmortizacion.PAGADO;
    }

    /**
     * Indica si tiene pagos parciales
     */
    public boolean isParcialmentePagada() {
        return estado == EstadoAmortizacion.PARCIALMENTE_PAGADO;
    }
}
