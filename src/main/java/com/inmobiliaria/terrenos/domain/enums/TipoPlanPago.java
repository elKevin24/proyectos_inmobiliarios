package com.inmobiliaria.terrenos.domain.enums;

public enum TipoPlanPago {
    CONTADO("Pago de contado"),
    FINANCIAMIENTO_PROPIO("Financiamiento propio de la inmobiliaria"),
    CREDITO_BANCARIO("Crédito bancario o hipotecario"),
    MIXTO("Combinación de métodos");

    private final String descripcion;

    TipoPlanPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
