package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Clientes.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class ClienteException extends BusinessException {

    public ClienteException(String message) {
        super(message, "CLIENTE_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ClienteException(String message, HttpStatus status) {
        super(message, "CLIENTE_ERROR", status);
    }

    public ClienteException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static ClienteException duplicado(String campo, String valor) {
        return new ClienteException(
                "Ya existe un cliente con el " + campo + ": " + valor,
                "CLIENTE_DUPLICADO",
                HttpStatus.CONFLICT
        );
    }

    public static ClienteException tieneCotizacionesVigentes() {
        return new ClienteException(
                "No se puede eliminar el cliente porque tiene cotizaciones vigentes",
                "CLIENTE_COTIZACIONES_VIGENTES",
                HttpStatus.CONFLICT
        );
    }

    public static ClienteException tieneApartadosVigentes() {
        return new ClienteException(
                "No se puede eliminar el cliente porque tiene apartados vigentes",
                "CLIENTE_APARTADOS_VIGENTES",
                HttpStatus.CONFLICT
        );
    }

    public static ClienteException tieneVentasActivas() {
        return new ClienteException(
                "No se puede eliminar el cliente porque tiene ventas activas",
                "CLIENTE_VENTAS_ACTIVAS",
                HttpStatus.CONFLICT
        );
    }
}
