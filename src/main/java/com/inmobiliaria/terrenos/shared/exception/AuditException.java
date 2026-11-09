package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Auditoría.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class AuditException extends BusinessException {

    public AuditException(String message) {
        super(message, "AUDIT_ERROR", HttpStatus.BAD_REQUEST);
    }

    public AuditException(String message, HttpStatus status) {
        super(message, "AUDIT_ERROR", status);
    }

    public AuditException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static AuditException tenantNoEncontrado() {
        return new AuditException(
                "No se encontro tenant_id en el contexto",
                "AUDIT_TENANT_NO_ENCONTRADO",
                HttpStatus.UNAUTHORIZED
        );
    }
}
