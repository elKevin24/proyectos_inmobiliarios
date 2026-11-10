package com.inmobiliaria.terrenos.application.port.out;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Puerto de salida para desacoplar el almacenamiento de archivos (DIP y OCP).
 */
public interface FileStoragePort {
    Path store(MultipartFile file, String subDirectory, String targetFilename) throws IOException;
    Resource loadAsResource(String storedPath);
}
