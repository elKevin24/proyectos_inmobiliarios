package com.inmobiliaria.terrenos.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados posibles de un terreno/lote.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum EstadoTerreno {

    DISPONIBLE("Disponible", "Terreno disponible para la venta"),
    APARTADO("Apartado", "Terreno apartado/reservado temporalmente"),
    VENDIDO("Vendido", "Terreno vendido"),
    RESERVADO("Reservado", "Terreno reservado por la empresa");

    private final String nombre;
    private final String descripcion;
}
