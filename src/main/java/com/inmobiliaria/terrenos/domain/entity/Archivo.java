package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad para gestión de archivos (planos, imágenes, documentos)
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "archivos", indexes = {
        @Index(name = "idx_archivo_tenant", columnList = "tenant_id"),
        @Index(name = "idx_archivo_proyecto", columnList = "proyecto_id"),
        @Index(name = "idx_archivo_terreno", columnList = "terreno_id"),
        @Index(name = "idx_archivo_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Archivo extends TenantBaseEntity {

    @Column(name = "proyecto_id")
    private Long proyectoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", insertable = false, updatable = false)
    private Proyecto proyecto;

    @Column(name = "terreno_id")
    private Long terrenoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terreno_id", insertable = false, updatable = false)
    private Terreno terreno;

    @Column(name = "venta_id")
    private Long ventaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", insertable = false, updatable = false)
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoArchivo tipo;

    @Column(nullable = false, length = 255)
    private String nombreOriginal;

    @Column(nullable = false, length = 500)
    private String nombreAlmacenado;

    @Column(nullable = false, length = 1000)
    private String ruta;

    @Column(length = 100)
    private String extension;

    @Column(length = 100)
    private String mimeType;

    private Long tamanioBytes;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String tags;

    @Builder.Default
    private Boolean esActivo = true;
}
