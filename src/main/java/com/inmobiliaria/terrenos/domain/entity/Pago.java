package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoPago;
import com.inmobiliaria.terrenos.domain.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para Pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "plan_pago_id", nullable = false)
    private Long planPagoId;

    @Column(name = "amortizacion_id")
    private Long amortizacionId;

    @Column(name = "cliente_id")
    private Long clienteId;

    // Información del pago
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(name = "monto_pagado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoPagado;

    // Distribución del pago
    @Column(name = "monto_a_capital", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montoACapital = BigDecimal.ZERO;

    @Column(name = "monto_a_interes", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montoAInteres = BigDecimal.ZERO;

    @Column(name = "monto_a_mora", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montoAMora = BigDecimal.ZERO;

    // Método de pago
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @Column(name = "referencia_pago", length = 100)
    private String referenciaPago;

    // Estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoPago estado = EstadoPago.APLICADO;

    // Información adicional
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "comprobante_ruta", length = 500)
    private String comprobanteRuta;

    @Column(name = "usuario_id")
    private Long usuarioId;

    // Auditoría
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
