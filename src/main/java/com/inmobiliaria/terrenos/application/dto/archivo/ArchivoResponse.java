package com.inmobiliaria.terrenos.application.dto.archivo;

import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para archivos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoResponse {

    private Long id;
    private Long tenantId;
    private Long proyectoId;
    private String proyectoNombre;
    private Long terrenoId;
    private String terrenoNumeroLote;
    private Long ventaId;
    private TipoArchivo tipo;
    private String nombreOriginal;
    private String nombreAlmacenado;
    private String extension;
    private String mimeType;
    private Long tamanioBytes;
    private Integer version;
    private String descripcion;
    private String tags;
    private Boolean esActivo;
    private String urlDescarga;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Formatea el tamaño en formato legible
     */
    public String getTamanioFormateado() {
        if (tamanioBytes == null) {
            return "0 B";
        }

        if (tamanioBytes < 1024) {
            return tamanioBytes + " B";
        } else if (tamanioBytes < 1024 * 1024) {
            return String.format("%.2f KB", tamanioBytes / 1024.0);
        } else if (tamanioBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", tamanioBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", tamanioBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Verifica si es una imagen
     */
    public boolean isImagen() {
        return tipo == TipoArchivo.IMAGEN_PROYECTO || tipo == TipoArchivo.IMAGEN_TERRENO;
    }

    /**
     * Verifica si es un plano
     */
    public boolean isPlano() {
        return tipo == TipoArchivo.PLANO_PROYECTO || tipo == TipoArchivo.PLANO_TERRENO;
    }
}
