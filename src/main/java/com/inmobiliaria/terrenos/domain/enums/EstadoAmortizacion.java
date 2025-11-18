package com.inmobiliaria.terrenos.domain.enums;

public enum EstadoAmortizacion {
    PENDIENTE("Pendiente de pago"),
    PAGADO("Pagado completamente"),
    VENCIDO("Vencido - con atraso"),
    PARCIALMENTE_PAGADO("Parcialmente pagado");

    private final String descripcion;

    EstadoAmortizacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
