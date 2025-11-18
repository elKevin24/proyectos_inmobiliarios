package com.inmobiliaria.terrenos.domain.enums;

/**
 * Enum para el estado del cliente en el funnel de ventas
 *
 * @author Kevin
 * @version 1.0.0
 */
public enum EstadoCliente {
    PROSPECTO("Prospecto - Primer contacto"),
    INTERESADO("Interesado - Ha solicitado información"),
    COMPRADOR("Comprador - Ha realizado una compra"),
    INACTIVO("Inactivo - Sin actividad reciente");

    private final String descripcion;

    EstadoCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
