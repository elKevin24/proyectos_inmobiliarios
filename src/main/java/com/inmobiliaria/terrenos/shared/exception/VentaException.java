package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Ventas.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class VentaException extends BusinessException {

    public VentaException(String message) {
        super(message, "VENTA_ERROR", HttpStatus.BAD_REQUEST);
    }

    public VentaException(String message, HttpStatus status) {
        super(message, "VENTA_ERROR", status);
    }

    public VentaException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static VentaException terrenoYaVendido() {
        return new VentaException(
                "El terreno ya ha sido vendido",
                "VENTA_TERRENO_YA_VENDIDO",
                HttpStatus.CONFLICT
        );
    }

    public static VentaException apartadoNoVigente() {
        return new VentaException(
                "El apartado debe estar vigente para convertirlo en venta",
                "VENTA_APARTADO_NO_VIGENTE",
                HttpStatus.CONFLICT
        );
    }

    public static VentaException soloEliminarCanceladas() {
        return new VentaException(
                "Solo se pueden eliminar ventas canceladas",
                "VENTA_SOLO_ELIMINAR_CANCELADAS",
                HttpStatus.CONFLICT
        );
    }

    public static VentaException estadoInvalido(String estadoActual, String estadoDeseado) {
        return new VentaException(
                "No se puede cambiar de estado " + estadoActual + " a " + estadoDeseado,
                "VENTA_ESTADO_INVALIDO",
                HttpStatus.CONFLICT
        );
    }
}
