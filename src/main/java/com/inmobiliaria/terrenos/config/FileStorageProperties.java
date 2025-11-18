package com.inmobiliaria.terrenos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para almacenamiento de archivos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Getter
@Setter
public class FileStorageProperties {

    private String uploadDir = "uploads";
    private long maxFileSize = 10485760; // 10MB por defecto
    private String[] allowedExtensions = {"pdf", "png", "jpg", "jpeg", "dwg", "dxf", "doc", "docx"};
}
