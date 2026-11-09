package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Apartados.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class ApartadoException extends BusinessException {

    public ApartadoException(String message) {
        super(message, "APARTADO_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ApartadoException(String message, HttpStatus status) {
        super(message, "APARTADO_ERROR", status);
    }

    public ApartadoException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static ApartadoException terrenoNoDisponible(String estadoActual) {
        return new ApartadoException(
                "El terreno no esta disponible para apartar. Estado actual: " + estadoActual,
                "APARTADO_TERRENO_NO_DISPONIBLE",
                HttpStatus.CONFLICT
        );
    }

    public static ApartadoException montoExcedePrecio() {
        return new ApartadoException(
                "El monto de apartado no puede ser mayor al precio total",
                "APARTADO_MONTO_EXCEDE_PRECIO",
                HttpStatus.BAD_REQUEST
        );
    }

    public static ApartadoException soloCancelarVigentes(String estadoActual) {
        return new ApartadoException(
                "Solo se pueden cancelar apartados vigentes. Estado actual: " + estadoActual,
                "APARTADO_SOLO_CANCELAR_VIGENTES",
                HttpStatus.CONFLICT
        );
    }

    public static ApartadoException noEliminarVigente() {
        return new ApartadoException(
                "No se puede eliminar un apartado vigente. Primero debe cancelarlo.",
                "APARTADO_NO_ELIMINAR_VIGENTE",
                HttpStatus.CONFLICT
        );
    }
}
