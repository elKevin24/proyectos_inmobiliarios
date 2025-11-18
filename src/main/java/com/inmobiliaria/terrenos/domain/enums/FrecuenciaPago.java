package com.inmobiliaria.terrenos.domain.enums;

public enum FrecuenciaPago {
    SEMANAL("Semanal", 7),
    QUINCENAL("Quincenal", 15),
    MENSUAL("Mensual", 30),
    BIMESTRAL("Bimestral", 60),
    TRIMESTRAL("Trimestral", 90),
    SEMESTRAL("Semestral", 180),
    ANUAL("Anual", 365);

    private final String descripcion;
    private final int dias;

    FrecuenciaPago(String descripcion, int dias) {
        this.descripcion = descripcion;
        this.dias = dias;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDias() {
        return dias;
    }
}
