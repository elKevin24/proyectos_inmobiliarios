package com.inmobiliaria.terrenos.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base para errores de lógica de negocio.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String message, String code, HttpStatus status) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
        this.code = "BUSINESS_ERROR";
    }
}
