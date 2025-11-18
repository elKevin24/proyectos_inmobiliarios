package com.inmobiliaria.terrenos.domain.enums;

/**
 * Enum para el origen del cliente (cómo conoció el proyecto)
 *
 * @author Kevin
 * @version 1.0.0
 */
public enum OrigenCliente {
    REFERIDO("Referido por otro cliente"),
    REDES_SOCIALES("Redes Sociales"),
    VISITA_DIRECTA("Visita Directa al Proyecto"),
    PUBLICIDAD("Publicidad (TV, Radio, Prensa)"),
    RECOMENDACION("Recomendación"),
    EVENTO("Evento o Feria"),
    OTRO("Otro");

    private final String descripcion;

    OrigenCliente(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
