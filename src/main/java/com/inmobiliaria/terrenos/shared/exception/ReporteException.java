package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Reportes.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class ReporteException extends BusinessException {

    public ReporteException(String message) {
        super(message, "REPORTE_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ReporteException(String message, HttpStatus status) {
        super(message, "REPORTE_ERROR", status);
    }

    public ReporteException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static ReporteException datosNoEncontrados() {
        return new ReporteException(
                "No se encontraron datos para generar el reporte",
                "REPORTE_SIN_DATOS",
                HttpStatus.NOT_FOUND
        );
    }
}
