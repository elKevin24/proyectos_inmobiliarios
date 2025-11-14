package com.inmobiliaria.terrenos.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Estados posibles de una venta.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
@RequiredArgsConstructor
public enum EstadoVenta {

    PENDIENTE("Pendiente", "Venta registrada, pago pendiente"),
    PAGADO("Pagado", "Venta completada y pagada"),
    CANCELADA("Cancelada", "Venta cancelada/revertida"),
    ANULADA("Anulada", "Venta anulada por error administrativo");

    private final String nombre;
    private final String descripcion;
}
