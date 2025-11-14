package com.inmobiliaria.terrenos.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Clase base para entidades que requieren multi-tenancy.
 * Añade el campo tenant_id a las entidades que heredan de esta clase.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
@Setter
@MappedSuperclass
public abstract class TenantBaseEntity extends BaseEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;
}
