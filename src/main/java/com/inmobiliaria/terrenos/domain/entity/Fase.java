package com.inmobiliaria.terrenos.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA para Fases de Proyectos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "fases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted = false")
public class Fase extends TenantBaseEntity {

    @Column(name = "proyecto_id", nullable = false)
    private Long proyectoId;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "numero_fase", nullable = false)
    private Integer numeroFase;

    @Column(name = "total_terrenos")
    private Integer totalTerrenos;

    @Column(name = "terrenos_disponibles")
    private Integer terrenosDisponibles;

    @Column(name = "area_total", precision = 15, scale = 2)
    private BigDecimal areaTotal;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", insertable = false, updatable = false)
    private Proyecto proyecto;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "fase_id", insertable = false, updatable = false)
    private java.util.List<Terreno> terrenos;
}
