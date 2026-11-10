package com.inmobiliaria.terrenos.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA para Cotizaciones
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "cotizaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted = false")
public class Cotizacion extends TenantBaseEntity {

    @Column(name = "terreno_id", nullable = false)
    private Long terrenoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terreno_id", insertable = false, updatable = false)
    private Terreno terreno;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 255)
    private String clienteNombre;

    @Column(name = "cliente_email", length = 255)
    private String clienteEmail;

    @Column(name = "cliente_telefono", length = 20)
    private String clienteTelefono;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "descuento", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "porcentaje_descuento", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(name = "precio_final", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioFinal;

    @Column(name = "fecha_vigencia")
    private LocalDate fechaVigencia;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
