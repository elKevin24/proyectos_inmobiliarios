package com.inmobiliaria.terrenos.domain.enums;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TRANSFERENCIA("Transferencia bancaria"),
    CHEQUE("Cheque"),
    TARJETA_CREDITO("Tarjeta de crédito"),
    TARJETA_DEBITO("Tarjeta de débito"),
    OTRO("Otro método");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
