package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Planos.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class PlanoException extends BusinessException {

    public PlanoException(String message) {
        super(message, "PLANO_ERROR", HttpStatus.BAD_REQUEST);
    }

    public PlanoException(String message, HttpStatus status) {
        super(message, "PLANO_ERROR", status);
    }

    public PlanoException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static PlanoException archivoVacio() {
        return new PlanoException(
                "El archivo esta vacio",
                "PLANO_ARCHIVO_VACIO",
                HttpStatus.BAD_REQUEST
        );
    }

    public static PlanoException archivoExcedeLimite(long limiteMB) {
        return new PlanoException(
                "El archivo supera el limite de " + limiteMB + "MB",
                "PLANO_ARCHIVO_EXCEDE_LIMITE",
                HttpStatus.BAD_REQUEST
        );
    }

    public static PlanoException analisisFallido(String detalle) {
        return new PlanoException(
                "Error en el analisis del plano: " + detalle,
                "PLANO_ANALISIS_FALLIDO",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
