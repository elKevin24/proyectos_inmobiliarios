package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando no se encuentra el tenant_id en el request.
 *
 * @author Kevin
 * @version 1.0.0
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }

    public TenantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
