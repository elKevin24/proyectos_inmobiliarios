package com.inmobiliaria.terrenos.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para errores del módulo de Archivos.
 *
 * @author Kevin
 * @version 1.0.0
 */
public class ArchivoException extends BusinessException {

    public ArchivoException(String message) {
        super(message, "ARCHIVO_ERROR", HttpStatus.BAD_REQUEST);
    }

    public ArchivoException(String message, HttpStatus status) {
        super(message, "ARCHIVO_ERROR", status);
    }

    public ArchivoException(String message, String code, HttpStatus status) {
        super(message, code, status);
    }

    // Errores comunes del módulo
    public static ArchivoException archivoVacio() {
        return new ArchivoException(
                "El archivo esta vacio",
                "ARCHIVO_VACIO",
                HttpStatus.BAD_REQUEST
        );
    }

    public static ArchivoException tamanoExcedido(long tamanoMaximo) {
        return new ArchivoException(
                "El archivo excede el tamano maximo permitido: " + tamanoMaximo + " bytes",
                "ARCHIVO_TAMANO_EXCEDIDO",
                HttpStatus.BAD_REQUEST
        );
    }

    public static ArchivoException tipoNoPermitido(String tipo) {
        return new ArchivoException(
                "Tipo de archivo no permitido: " + tipo,
                "ARCHIVO_TIPO_NO_PERMITIDO",
                HttpStatus.BAD_REQUEST
        );
    }

    public static ArchivoException noEncontrado() {
        return new ArchivoException(
                "El archivo no existe o no se puede leer",
                "ARCHIVO_NO_ENCONTRADO",
                HttpStatus.NOT_FOUND
        );
    }

    public static ArchivoException errorAlmacenamiento(String detalle) {
        return new ArchivoException(
                "Error al almacenar el archivo: " + detalle,
                "ARCHIVO_ERROR_ALMACENAMIENTO",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
