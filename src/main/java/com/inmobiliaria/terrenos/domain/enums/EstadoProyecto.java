package com.inmobiliaria.terrenos.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados posibles de un proyecto inmobiliario.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum EstadoProyecto {

    PLANIFICACION("Planificación", "Proyecto en fase de planificación"),
    EN_VENTA("En Venta", "Proyecto activo con terrenos disponibles"),
    AGOTADO("Agotado", "Todos los terrenos vendidos"),
    SUSPENDIDO("Suspendido", "Ventas temporalmente suspendidas"),
    CANCELADO("Cancelado", "Proyecto cancelado");

    private final String nombre;
    private final String descripcion;
}
