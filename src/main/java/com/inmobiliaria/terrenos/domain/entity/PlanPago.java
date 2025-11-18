package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.FrecuenciaPago;
import com.inmobiliaria.terrenos.domain.enums.TipoPlanPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para Plan de Pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "planes_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class PlanPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "cliente_id")
    private Long clienteId;

    // Configuración del plan
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_plan", nullable = false, length = 30)
    private TipoPlanPago tipoPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "frecuencia_pago", nullable = false, length = 20)
    @Builder.Default
    private FrecuenciaPago frecuenciaPago = FrecuenciaPago.MENSUAL;

    // Montos
    @Column(name = "monto_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal enganche = BigDecimal.ZERO;

    @Column(name = "monto_financiado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoFinanciado;

    // Intereses
    @Column(name = "tasa_interes_anual", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tasaInteresAnual = BigDecimal.ZERO;

    @Column(name = "tasa_interes_mensual", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal tasaInteresMensual = BigDecimal.ZERO;

    @Column(name = "aplica_interes", nullable = false)
    @Builder.Default
    private Boolean aplicaInteres = false;

    // Plazos
    @Column(name = "numero_pagos", nullable = false)
    private Integer numeroPagos;

    @Column(name = "plazo_meses")
    private Integer plazoMeses;

    // Mora
    @Column(name = "tasa_mora_mensual", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tasaMoraMensual = BigDecimal.ZERO;

    @Column(name = "dias_gracia")
    @Builder.Default
    private Integer diasGracia = 0;

    // Fechas
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_primer_pago", nullable = false)
    private LocalDate fechaPrimerPago;

    @Column(name = "fecha_ultimo_pago")
    private LocalDate fechaUltimoPago;

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

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Calcular monto financiado si no está establecido
        if (montoFinanciado == null && montoTotal != null) {
            montoFinanciado = montoTotal.subtract(enganche != null ? enganche : BigDecimal.ZERO);
        }

        // Calcular tasa mensual desde anual
        if (tasaInteresAnual != null && tasaInteresAnual.compareTo(BigDecimal.ZERO) > 0) {
            tasaInteresMensual = tasaInteresAnual.divide(new BigDecimal("12"), 4, java.math.RoundingMode.HALF_UP);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
