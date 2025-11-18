package com.inmobiliaria.terrenos.domain.enums;

/**
 * Tipos de archivos que se pueden almacenar
 *
 * @author Kevin
 * @version 1.0.0
 */
public enum TipoArchivo {
    PLANO_PROYECTO("Plano del proyecto"),
    PLANO_TERRENO("Plano del terreno/lote"),
    IMAGEN_PROYECTO("Imagen del proyecto"),
    IMAGEN_TERRENO("Imagen del terreno"),
    DOCUMENTO_PROYECTO("Documento del proyecto"),
    DOCUMENTO_TERRENO("Documento del terreno"),
    DOCUMENTO_VENTA("Documento de venta"),
    CONTRATO("Contrato"),
    ESCRITURA("Escritura"),
    OTRO("Otro tipo de archivo");

    private final String descripcion;

    TipoArchivo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
