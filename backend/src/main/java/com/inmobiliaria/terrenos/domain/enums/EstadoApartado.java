package com.inmobiliaria.terrenos.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados posibles de un apartado/reserva.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum EstadoApartado {

    ACTIVO("Activo", "Apartado vigente dentro del plazo"),
    COMPLETADO("Completado", "Se convirtió en venta"),
    VENCIDO("Vencido", "Plazo de apartado expiró"),
    CANCELADO("Cancelado", "Cliente canceló el apartado");

    private final String nombre;
    private final String descripcion;
}
