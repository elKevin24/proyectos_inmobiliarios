package com.inmobiliaria.terrenos.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entidad Tenant (Empresa/Cliente del sistema SaaS).
 * Representa una empresa que utiliza el sistema.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_email", columnList = "email"),
        @Index(name = "idx_tenant_activo", columnList = "activo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends BaseEntity {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Size(max = 500)
    @Column(length = 500)
    private String direccion;

    @Size(max = 100)
    @Column(name = "razon_social", length = 100)
    private String razonSocial;

    @Size(max = 50)
    @Column(length = 50)
    private String rfc;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "max_usuarios")
    @Builder.Default
    private Integer maxUsuarios = 10;

    @Column(name = "max_proyectos")
    @Builder.Default
    private Integer maxProyectos = 5;
}
