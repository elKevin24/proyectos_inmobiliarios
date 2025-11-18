package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.archivo.ArchivoResponse;
import com.inmobiliaria.terrenos.config.FileStorageProperties;
import com.inmobiliaria.terrenos.domain.entity.Archivo;
import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import com.inmobiliaria.terrenos.domain.repository.ArchivoRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.ArchivoMapper;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de gestión de archivos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArchivoService {

    private final ArchivoRepository archivoRepository;
    private final ProyectoRepository proyectoRepository;
    private final TerrenoRepository terrenoRepository;
    private final ArchivoMapper archivoMapper;
    private final FileStorageProperties fileStorageProperties;

    private Path fileStorageLocation;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    private Path getFileStorageLocation() {
        if (fileStorageLocation == null) {
            fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                    .toAbsolutePath().normalize();
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (IOException ex) {
                throw new BusinessException("No se pudo crear el directorio de almacenamiento", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return fileStorageLocation;
    }

    /**
     * Sube un archivo
     */
    @Transactional
    public ArchivoResponse subirArchivo(MultipartFile file, TipoArchivo tipo, Long proyectoId, Long terrenoId, String descripcion) {
        Long tenantId = getTenantId();
        log.info("Subiendo archivo '{}' tipo {} para tenant: {}", file.getOriginalFilename(), tipo, tenantId);

        // Validar proyecto si se proporciona
        if (proyectoId != null) {
            proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + proyectoId));
        }

        // Validar terreno si se proporciona
        if (terrenoId != null) {
            terrenoRepository.findByIdAndTenantIdAndDeletedFalse(terrenoId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Terreno no encontrado con id: " + terrenoId));
        }

        // Validar archivo
        validarArchivo(file);

        // Guardar archivo físicamente
        String nombreOriginal = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(nombreOriginal);
        String nombreAlmacenado = generarNombreUnico(nombreOriginal);
        Path targetLocation = getFileStorageLocation().resolve(nombreAlmacenado);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            log.error("Error al guardar archivo: {}", ex.getMessage());
            throw new BusinessException("No se pudo almacenar el archivo", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Determinar versión
        Integer version = 1;
        if (proyectoId != null) {
            archivoRepository.findUltimaVersion(tenantId, proyectoId, tipo, nombreOriginal)
                    .ifPresent(ultimoArchivo -> {
                        // Desactivar versión anterior
                        ultimoArchivo.setEsActivo(false);
                        archivoRepository.save(ultimoArchivo);
                    });

            List<Archivo> versiones = archivoRepository.findTodasLasVersiones(tenantId, proyectoId, nombreOriginal);
            if (!versiones.isEmpty()) {
                version = versiones.get(0).getVersion() + 1;
            }
        }

        // Guardar metadata en BD
        Archivo archivo = Archivo.builder()
                .tenantId(tenantId)
                .proyectoId(proyectoId)
                .terrenoId(terrenoId)
                .tipo(tipo)
                .nombreOriginal(nombreOriginal)
                .nombreAlmacenado(nombreAlmacenado)
                .ruta(targetLocation.toString())
                .extension(extension)
                .mimeType(file.getContentType())
                .tamanioBytes(file.getSize())
                .version(version)
                .descripcion(descripcion)
                .esActivo(true)
                .build();

        Archivo archivoGuardado = archivoRepository.save(archivo);
        log.info("Archivo guardado con id: {} (versión {})", archivoGuardado.getId(), version);

        return archivoMapper.toResponse(archivoGuardado);
    }

    /**
     * Descarga un archivo
     */
    @Transactional(readOnly = true)
    public Resource descargarArchivo(Long id) {
        Long tenantId = getTenantId();
        log.debug("Descargando archivo {} para tenant: {}", id, tenantId);

        Archivo archivo = archivoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado con id: " + id));

        try {
            Path filePath = Paths.get(archivo.getRuta()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("El archivo no existe o no se puede leer", HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException ex) {
            log.error("Error al leer archivo: {}", ex.getMessage());
            throw new BusinessException("Error al descargar el archivo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Lista archivos
     */
    @Transactional(readOnly = true)
    public List<ArchivoResponse> listarArchivos(Long proyectoId, Long terrenoId, TipoArchivo tipo) {
        Long tenantId = getTenantId();
        log.debug("Listando archivos para tenant: {}", tenantId);

        List<Archivo> archivos;
        if (proyectoId != null && tipo != null) {
            archivos = archivoRepository.findByProyectoYTipo(tenantId, proyectoId, tipo);
        } else if (proyectoId != null) {
            archivos = archivoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByVersionDesc(tenantId, proyectoId);
        } else if (terrenoId != null) {
            archivos = archivoRepository.findByTenantIdAndTerrenoIdAndDeletedFalseOrderByVersionDesc(tenantId, terrenoId);
        } else if (tipo != null) {
            archivos = archivoRepository.findByTenantIdAndTipoAndDeletedFalseOrderByCreatedAtDesc(tenantId, tipo);
        } else {
            archivos = archivoRepository.findByTenantIdAndDeletedFalse(tenantId);
        }

        return archivoMapper.toResponseList(archivos);
    }

    /**
     * Obtiene galería de imágenes de un proyecto
     */
    @Transactional(readOnly = true)
    public List<ArchivoResponse> obtenerGaleria(Long proyectoId) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo galería del proyecto {} para tenant: {}", proyectoId, tenantId);

        List<Archivo> imagenes = archivoRepository.findGaleriaProyecto(tenantId, proyectoId);
        return archivoMapper.toResponseList(imagenes);
    }

    /**
     * Obtiene todas las versiones de un archivo
     */
    @Transactional(readOnly = true)
    public List<ArchivoResponse> obtenerVersiones(Long proyectoId, String nombreOriginal) {
        Long tenantId = getTenantId();
        log.debug("Obteniendo versiones de '{}' del proyecto {} para tenant: {}", nombreOriginal, proyectoId, tenantId);

        List<Archivo> versiones = archivoRepository.findTodasLasVersiones(tenantId, proyectoId, nombreOriginal);
        return archivoMapper.toResponseList(versiones);
    }

    /**
     * Elimina un archivo (soft delete)
     */
    @Transactional
    public void eliminarArchivo(Long id) {
        Long tenantId = getTenantId();
        log.info("Eliminando archivo {} para tenant: {}", id, tenantId);

        Archivo archivo = archivoRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado con id: " + id));

        archivo.setDeleted(true);
        archivo.setEsActivo(false);
        archivoRepository.save(archivo);

        log.info("Archivo {} eliminado exitosamente", id);
    }

    /**
     * Valida el archivo
     */
    private void validarArchivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("El archivo está vacío", HttpStatus.BAD_REQUEST);
        }

        // Validar tamaño
        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new BusinessException("El archivo excede el tamaño máximo permitido", HttpStatus.BAD_REQUEST);
        }

        // Validar extensión
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(filename).toLowerCase();

        if (!Arrays.asList(fileStorageProperties.getAllowedExtensions()).contains(extension)) {
            throw new BusinessException("Tipo de archivo no permitido: " + extension, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex >= 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * Genera un nombre único para el archivo
     */
    private String generarNombreUnico(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }
}
