package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para Apartados/Reservas de terrenos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "apartados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted = false")
public class Apartado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "terreno_id", nullable = false)
    private Long terrenoId;

    @Column(name = "cotizacion_id")
    private Long cotizacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terreno_id", insertable = false, updatable = false)
    private Terreno terreno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id", insertable = false, updatable = false)
    private Cotizacion cotizacion;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "monto_apartado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoApartado;

    @Column(name = "precio_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "fecha_apartado", nullable = false)
    private LocalDate fechaApartado;

    @Column(name = "duracion_dias", nullable = false)
    @Builder.Default
    private Integer duracionDias = 30;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoApartado estado = EstadoApartado.VIGENTE;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
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
