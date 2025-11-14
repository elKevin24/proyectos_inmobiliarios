package com.inmobiliaria.terrenos.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipo de configuración de precio de un proyecto.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum TipoPrecio {

    FIJO("Precio Fijo", "Todos los terrenos tienen el mismo precio base"),
    VARIABLE("Precio Variable", "Precio por m² con ajustes por ubicación/características");

    private final String nombre;
    private final String descripcion;
}
