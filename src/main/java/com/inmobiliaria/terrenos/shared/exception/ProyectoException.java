package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Proyectos.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class ProyectoException extends BusinessException {

    public ProyectoException(String message) {
        super(message, "PROYECTO_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ProyectoException(String message, HttpStatus status) {
        super(message, "PROYECTO_ERROR", status);
    }

    public ProyectoException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static ProyectoException nombreDuplicado(String nombre) {
        return new ProyectoException(
                "Ya existe un proyecto con el nombre: " + nombre,
                "PROYECTO_NOMBRE_DUPLICADO",
                HttpStatus.CONFLICT
        );
    }

    public static ProyectoException tieneTerrenosVendidos() {
        return new ProyectoException(
                "No se puede eliminar el proyecto porque tiene terrenos vendidos",
                "PROYECTO_TERRENOS_VENDIDOS",
                HttpStatus.CONFLICT
        );
    }

    public static ProyectoException tieneTerrenosApartados() {
        return new ProyectoException(
                "No se puede eliminar el proyecto porque tiene terrenos apartados",
                "PROYECTO_TERRENOS_APARTADOS",
                HttpStatus.CONFLICT
        );
    }

    public static ProyectoException sinTerrenos() {
        return new ProyectoException(
                "No se puede poner en venta un proyecto sin terrenos",
                "PROYECTO_SIN_TERRENOS",
                HttpStatus.BAD_REQUEST
        );
    }

    public static ProyectoException estadoInvalido(String estadoActual, String estadoDeseado) {
        return new ProyectoException(
                "No se puede cambiar de estado " + estadoActual + " a " + estadoDeseado,
                "PROYECTO_ESTADO_INVALIDO",
                HttpStatus.CONFLICT
        );
    }

    public static ProyectoException tieneApartadosActivos() {
        return new ProyectoException(
                "No se puede finalizar el proyecto con terrenos apartados",
                "PROYECTO_APARTADOS_ACTIVOS",
                HttpStatus.CONFLICT
        );
    }
}
