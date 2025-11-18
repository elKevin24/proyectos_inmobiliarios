package com.inmobiliaria.terrenos.application.dto.pago;

import com.inmobiliaria.terrenos.domain.enums.EstadoPago;
import com.inmobiliaria.terrenos.domain.enums.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

    private Long id;
    private Long planPagoId;
    private Long amortizacionId;
    private Long clienteId;

    // Información del pago
    private LocalDate fechaPago;
    private BigDecimal montoPagado;

    // Distribución del pago
    private BigDecimal montoACapital;
    private BigDecimal montoAInteres;
    private BigDecimal montoAMora;

    // Método de pago
    private MetodoPago metodoPago;
    private String metodoPagoDescripcion;
    private String referenciaPago;

    // Estado
    private EstadoPago estado;
    private String estadoDescripcion;

    // Información adicional
    private String observaciones;
    private String comprobanteRuta;
    private Long usuarioId;
    private String usuarioNombre;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /**
     * Indica si el pago está aplicado
     */
    public boolean isAplicado() {
        return estado == EstadoPago.APLICADO;
    }

    /**
     * Indica si el pago fue cancelado
     */
    public boolean isCancelado() {
        return estado == EstadoPago.CANCELADO;
    }

    /**
     * Indica si el pago fue reembolsado
     */
    public boolean isReembolsado() {
        return estado == EstadoPago.REEMBOLSADO;
    }
}
