package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

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
@SuperBuilder
@SQLRestriction("deleted = false")
public class Amortizacion extends TenantBaseEntity {

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

    @PrePersist
    protected void onAmortizacionCreate() {
        // Inicializar monto pendiente
        if (montoPendiente == null) {
            montoPendiente = montoCuota;
        }
    }
}
