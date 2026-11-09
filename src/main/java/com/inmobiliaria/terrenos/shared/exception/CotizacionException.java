package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Cotizaciones.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class CotizacionException extends BusinessException {

    public CotizacionException(String message) {
        super(message, "COTIZACION_ERROR", HttpStatus.BAD_REQUEST);
    }

    public CotizacionException(String message, HttpStatus status) {
        super(message, "COTIZACION_ERROR", status);
    }

    public CotizacionException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static CotizacionException terrenoNoDisponible() {
        return new CotizacionException(
                "El terreno no esta disponible para cotizar",
                "COTIZACION_TERRENO_NO_DISPONIBLE",
                HttpStatus.CONFLICT
        );
    }

    public static CotizacionException vencida() {
        return new CotizacionException(
                "La cotizacion ha vencido",
                "COTIZACION_VENCIDA",
                HttpStatus.CONFLICT
        );
    }
}
