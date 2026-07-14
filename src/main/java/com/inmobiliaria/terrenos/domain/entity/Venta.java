package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA para Ventas de terrenos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted = false")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "terreno_id", nullable = false)
    private Long terrenoId;

    @Column(name = "apartado_id")
    private Long apartadoId;

    @Column(name = "cotizacion_id")
    private Long cotizacionId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terreno_id", insertable = false, updatable = false)
    private Terreno terreno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartado_id", insertable = false, updatable = false)
    private Apartado apartado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", insertable = false, updatable = false)
    private Usuario usuario;

    // Datos del comprador (desnormalizados para histórico)
    @Column(name = "comprador_nombre", nullable = false, length = 200)
    private String compradorNombre;

    @Column(name = "comprador_email", length = 150)
    private String compradorEmail;

    @Column(name = "comprador_telefono", length = 20)
    private String compradorTelefono;

    @Column(name = "comprador_direccion", length = 255)
    private String compradorDireccion;

    @Column(name = "comprador_rfc", length = 13)
    private String compradorRfc;

    @Column(name = "comprador_curp", length = 18)
    private String compradorCurp;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    @Column(name = "precio_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "monto_apartado_acreditado", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoApartadoAcreditado = BigDecimal.ZERO;

    @Column(name = "monto_final", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoFinal;

    @Column(name = "porcentaje_comision", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeComision = BigDecimal.ZERO;

    @Column(name = "monto_comision", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoComision = BigDecimal.ZERO;

    @Column(name = "forma_pago", length = 30)
    private String formaPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoVenta estado = EstadoVenta.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

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
