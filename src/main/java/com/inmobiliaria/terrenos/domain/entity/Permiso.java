package com.inmobiliaria.terrenos.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad Permiso del sistema.
 * Define acciones granulares que pueden realizar los usuarios.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "permisos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permiso_codigo", columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String codigo; // Ej: "PROYECTO_CREATE", "VENTA_READ"

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String modulo; // Ej: "PROYECTO", "VENTA", "USUARIO"
}
