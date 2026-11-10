package com.inmobiliaria.terrenos.infrastructure.storage;

import com.inmobiliaria.terrenos.application.port.out.FileStoragePort;
import com.inmobiliaria.terrenos.config.FileStorageProperties;
import com.inmobiliaria.terrenos.shared.exception.ArchivoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageAdapter implements FileStoragePort {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public Path store(MultipartFile file, String subDirectory, String targetFilename) throws IOException {
        Path baseDir = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        Path targetDir = subDirectory != null && !subDirectory.isBlank() ? baseDir.resolve(subDirectory) : baseDir;
        Files.createDirectories(targetDir);

        Path targetLocation = targetDir.resolve(targetFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Archivo almacenado en: {}", targetLocation);
        return targetLocation;
    }

    @Override
    public Resource loadAsResource(String storedPath) {
        try {
            Path filePath = Paths.get(storedPath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw ArchivoException.noEncontrado();
            }
        } catch (MalformedURLException ex) {
            log.error("Error al leer archivo {}: {}", storedPath, ex.getMessage());
            throw ArchivoException.errorAlmacenamiento("Error al descargar el archivo");
        }
    }
}
