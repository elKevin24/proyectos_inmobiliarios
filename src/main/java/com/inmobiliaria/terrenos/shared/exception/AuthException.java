package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Autenticación.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class AuthException extends BusinessException {

    public AuthException(String message) {
        super(message, "AUTH_ERROR", HttpStatus.UNAUTHORIZED);
    }

    public AuthException(String message, HttpStatus status) {
        super(message, "AUTH_ERROR", status);
    }

    public AuthException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static AuthException usuarioDesactivado() {
        return new AuthException(
                "Usuario desactivado",
                "AUTH_USUARIO_DESACTIVADO",
                HttpStatus.FORBIDDEN
        );
    }

    public static AuthException empresaDesactivada() {
        return new AuthException(
                "Empresa desactivada. Contacte al administrador.",
                "AUTH_EMPRESA_DESACTIVADA",
                HttpStatus.FORBIDDEN
        );
    }

    public static AuthException empresaDuplicada(String campo, String valor) {
        return new AuthException(
                "Ya existe una empresa con ese " + campo + ": " + valor,
                "AUTH_EMPRESA_DUPLICADA",
                HttpStatus.CONFLICT
        );
    }

    public static AuthException usuarioDuplicado(String email) {
        return new AuthException(
                "Ya existe un usuario con ese email: " + email,
                "AUTH_USUARIO_DUPLICADO",
                HttpStatus.CONFLICT
        );
    }

    public static AuthException refreshTokenInvalido() {
        return new AuthException(
                "Refresh token invalido",
                "AUTH_REFRESH_TOKEN_INVALIDO",
                HttpStatus.UNAUTHORIZED
        );
    }

    public static AuthException refreshTokenExpirado() {
        return new AuthException(
                "Refresh token expirado o invalido",
                "AUTH_REFRESH_TOKEN_EXPIRADO",
                HttpStatus.UNAUTHORIZED
        );
    }
}
