package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Terrenos.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class TerrenoException extends BusinessException {

    public TerrenoException(String message) {
        super(message, "TERRENO_ERROR", HttpStatus.BAD_REQUEST);
    }

    public TerrenoException(String message, HttpStatus status) {
        super(message, "TERRENO_ERROR", status);
    }

    public TerrenoException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static TerrenoException loteDuplicado(String numeroLote) {
        return new TerrenoException(
                "Ya existe un terreno con el numero de lote: " + numeroLote,
                "TERRENO_LOTE_DUPLICADO",
                HttpStatus.CONFLICT
        );
    }

    public static TerrenoException noDisponible(String estadoActual) {
        return new TerrenoException(
                "El terreno no esta disponible. Estado actual: " + estadoActual,
                "TERRENO_NO_DISPONIBLE",
                HttpStatus.CONFLICT
        );
    }

    public static TerrenoException noEliminarVendido() {
        return new TerrenoException(
                "No se puede eliminar un terreno vendido",
                "TERRENO_NO_ELIMINAR_VENDIDO",
                HttpStatus.CONFLICT
        );
    }

    public static TerrenoException noEliminarDisponibleApartado() {
        return new TerrenoException(
                "No se puede eliminar un terreno que esta disponible o apartado",
                "TERRENO_NO_ELIMINAR_DISPONIBLE",
                HttpStatus.CONFLICT
        );
    }

    public static TerrenoException estadoInvalido(String estadoActual, String estadoDeseado) {
        return new TerrenoException(
                "No se puede cambiar de estado " + estadoActual + " a " + estadoDeseado,
                "TERRENO_ESTADO_INVALIDO",
                HttpStatus.CONFLICT
        );
    }

    public static TerrenoException ventaDirectaNoPermitida() {
        return new TerrenoException(
                "Un terreno disponible no puede venderse directamente. Primero debe apartarse.",
                "TERRENO_VENTA_DIRECTA_NO_PERMITIDA",
                HttpStatus.BAD_REQUEST
        );
    }
}
