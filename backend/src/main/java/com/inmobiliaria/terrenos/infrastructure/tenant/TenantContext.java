package com.inmobiliaria.terrenos.infrastructure.tenant;

import lombok.extern.slf4j.Slf4j;

/**
 * Contexto thread-local para almacenar el tenant_id de la petición actual.
 * Garantiza el aislamiento de datos entre diferentes tenants (empresas).
 *
 * @author Kevin
 * @version 1.0.0
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
        // Prevenir instanciación
    }

    /**
     * Establece el tenant_id para el thread actual.
     *
     * @param tenantId ID del tenant
     */
    public static void setTenantId(Long tenantId) {
        log.debug("Setting tenant_id: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Obtiene el tenant_id del thread actual.
     *
     * @return tenant_id o null si no está establecido
     */
    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Limpia el tenant_id del thread actual.
     * IMPORTANTE: Siempre llamar en finally para evitar memory leaks.
     */
    public static void clear() {
        log.debug("Clearing tenant context for thread: {}", Thread.currentThread().getName());
        CURRENT_TENANT.remove();
    }

    /**
     * Verifica si hay un tenant_id establecido.
     *
     * @return true si hay tenant_id
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
