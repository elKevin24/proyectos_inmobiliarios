package com.inmobiliaria.terrenos.domain.enums;

/**
 * Tipos de operaciones para auditoría crítica
 */
public enum TipoOperacionAudit {
    CREATE("Creación"),
    UPDATE("Actualización"),
    DELETE("Eliminación"),
    STATUS_CHANGE("Cambio de estado"),
    PRICE_CHANGE("Cambio de precio"),
    ASSIGNMENT("Asignación"),
    TRANSFER("Transferencia");

    private final String descripcion;

    TipoOperacionAudit(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
