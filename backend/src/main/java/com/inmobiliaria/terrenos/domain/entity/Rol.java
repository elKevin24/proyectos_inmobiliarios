package com.inmobiliaria.terrenos.domain.entity;

import com.inmobiliaria.terrenos.domain.enums.RolEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Rol del sistema.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rol_tenant", columnNames = {"nombre", "tenant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol extends TenantBaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RolEnum nombre;

    @Column(length = 200)
    private String descripcion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();
}
