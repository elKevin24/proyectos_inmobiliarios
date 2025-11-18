package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para Amortización (Cuota)
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "amortizaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Amortizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "plan_pago_id", nullable = false)
    private Long planPagoId;

    // Identificación
    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    // Montos de la cuota
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal capital;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal interes = BigDecimal.ZERO;

    @Column(name = "monto_cuota", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoCuota;

    // Montos pagados
    @Column(name = "monto_pagado", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "monto_pendiente", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoPendiente;

    // Mora
    @Column(name = "mora_acumulada", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal moraAcumulada = BigDecimal.ZERO;

    @Column(name = "dias_atraso")
    @Builder.Default
    private Integer diasAtraso = 0;

    // Fechas
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    // Estado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private EstadoAmortizacion estado = EstadoAmortizacion.PENDIENTE;

    // Saldo después del pago
    @Column(name = "saldo_restante", precision = 15, scale = 2)
    private BigDecimal saldoRestante;

    // Observaciones
    @Column(columnDefinition = "TEXT")
    private String notas;

    // Auditoría
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Inicializar monto pendiente
        if (montoPendiente == null) {
            montoPendiente = montoCuota;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
