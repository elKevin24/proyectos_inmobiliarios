package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import com.inmobiliaria.terrenos.domain.enums.TipoPrecio;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

/**
 * Entidad JPA para Proyectos Inmobiliarios
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "proyectos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLRestriction("deleted = false")
public class Proyecto extends TenantBaseEntity {

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String direccion;
    private String ciudad;
    private String estado;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    private Double latitud;
    private Double longitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_precio", length = 20)
    private TipoPrecio tipoPrecio;

    @Column(name = "precio_base", precision = 15, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "precio_maximo", precision = 15, scale = 2)
    private BigDecimal precioMaximo;

    @Column(name = "total_terrenos")
    private Integer totalTerrenos;

    @Column(name = "terrenos_disponibles")
    private Integer terrenosDisponibles;

    @Column(name = "terrenos_apartados")
    private Integer terrenosApartados;

    @Column(name = "terrenos_vendidos")
    private Integer terrenosVendidos;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_proyecto", length = 20)
    private EstadoProyecto estadoProyecto;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", insertable = false, updatable = false)
    private java.util.List<Terreno> terrenos;
}
