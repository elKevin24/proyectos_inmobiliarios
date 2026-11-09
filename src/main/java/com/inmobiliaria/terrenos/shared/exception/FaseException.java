package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Fases.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class FaseException extends BusinessException {

    public FaseException(String message) {
        super(message, "FASE_ERROR", HttpStatus.BAD_REQUEST);
    }

    public FaseException(String message, HttpStatus status) {
        super(message, "FASE_ERROR", status);
    }

    public FaseException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static FaseException nombreDuplicado(String nombre) {
        return new FaseException(
                "Ya existe una fase con el nombre: " + nombre,
                "FASE_NOMBRE_DUPLICADO",
                HttpStatus.CONFLICT
        );
    }

    public static FaseException numeroDuplicado(String numero) {
        return new FaseException(
                "Ya existe una fase con el numero: " + numero,
                "FASE_NUMERO_DUPLICADO",
                HttpStatus.CONFLICT
        );
    }

    public static FaseException tieneTerrenosVendidos() {
        return new FaseException(
                "No se puede eliminar la fase porque tiene terrenos vendidos",
                "FASE_TERRENOS_VENDIDOS",
                HttpStatus.CONFLICT
        );
    }

    public static FaseException tieneTerrenosApartados() {
        return new FaseException(
                "No se puede eliminar la fase porque tiene terrenos apartados",
                "FASE_TERRENOS_APARTADOS",
                HttpStatus.CONFLICT
        );
    }
}
