package com.inmobiliaria.terrenos.domain.enums;

public enum EstadoPago {
    APLICADO("Aplicado correctamente"),
    CANCELADO("Cancelado"),
    REEMBOLSADO("Reembolsado al cliente");

    private final String descripcion;

    EstadoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
