package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.archivo.ArchivoResponse;
import com.inmobiliaria.terrenos.application.service.ArchivoService;
import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para gestión de archivos
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/archivos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Archivos", description = "Gestión de archivos (planos, imágenes, documentos)")
@SecurityRequirement(name = "bearerAuth")
public class ArchivoController {

    private final ArchivoService archivoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ARCHIVO_CREAR', 'ADMIN')")
    @Operation(
            summary = "Subir archivo",
            description = "Sube un archivo (plano, imagen, documento) y lo vincula a un proyecto o terreno. " +
                         "Formatos permitidos: PDF, PNG, JPG, JPEG, DWG, DXF, DOC, DOCX. Máximo 10MB."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Archivo subido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ArchivoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Archivo inválido o tipo no permitido"),
            @ApiResponse(responseCode = "404", description = "Proyecto o terreno no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ArchivoResponse> subirArchivo(
            @Parameter(description = "Archivo a subir", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Tipo de archivo", required = true)
            @RequestParam("tipo") TipoArchivo tipo,

            @Parameter(description = "ID del proyecto (opcional)")
            @RequestParam(value = "proyectoId", required = false) Long proyectoId,

            @Parameter(description = "ID del terreno (opcional)")
            @RequestParam(value = "terrenoId", required = false) Long terrenoId,

            @Parameter(description = "Descripción del archivo (opcional)")
            @RequestParam(value = "descripcion", required = false) String descripcion
    ) {
        log.info("POST /api/v1/archivos/upload - archivo: {}, tipo: {}, proyectoId: {}, terrenoId: {}",
                file.getOriginalFilename(), tipo, proyectoId, terrenoId);

        ArchivoResponse response = archivoService.subirArchivo(file, tipo, proyectoId, terrenoId, descripcion);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ARCHIVO_VER', 'ADMIN')")
    @Operation(
            summary = "Descargar archivo",
            description = "Descarga un archivo por su ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo descargado"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Resource> descargarArchivo(
            @Parameter(description = "ID del archivo", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/archivos/{}/download", id);

        Resource resource = archivoService.descargarArchivo(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ARCHIVO_VER', 'ADMIN')")
    @Operation(
            summary = "Listar archivos",
            description = "Lista archivos con filtros opcionales (proyecto, terreno, tipo)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de archivos obtenida",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ArchivoResponse.class))
                    )
            )
    })
    public ResponseEntity<List<ArchivoResponse>> listarArchivos(
            @Parameter(description = "Filtrar por proyecto")
            @RequestParam(required = false) Long proyectoId,

            @Parameter(description = "Filtrar por terreno")
            @RequestParam(required = false) Long terrenoId,

            @Parameter(description = "Filtrar por tipo de archivo")
            @RequestParam(required = false) TipoArchivo tipo
    ) {
        log.info("GET /api/v1/archivos - proyectoId: {}, terrenoId: {}, tipo: {}", proyectoId, terrenoId, tipo);

        List<ArchivoResponse> archivos = archivoService.listarArchivos(proyectoId, terrenoId, tipo);
        return ResponseEntity.ok(archivos);
    }

    @GetMapping("/galeria/{proyectoId}")
    @PreAuthorize("hasAnyAuthority('ARCHIVO_VER', 'ADMIN')")
    @Operation(
            summary = "Obtener galería de imágenes",
            description = "Obtiene todas las imágenes activas de un proyecto"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Galería obtenida",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ArchivoResponse.class))
                    )
            )
    })
    public ResponseEntity<List<ArchivoResponse>> obtenerGaleria(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable Long proyectoId
    ) {
        log.info("GET /api/v1/archivos/galeria/{}", proyectoId);
        return ResponseEntity.ok(archivoService.obtenerGaleria(proyectoId));
    }

    @GetMapping("/versiones/{proyectoId}")
    @PreAuthorize("hasAnyAuthority('ARCHIVO_VER', 'ADMIN')")
    @Operation(
            summary = "Obtener versiones de un archivo",
            description = "Obtiene todas las versiones de un archivo específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Versiones obtenidas",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ArchivoResponse.class))
                    )
            )
    })
    public ResponseEntity<List<ArchivoResponse>> obtenerVersiones(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable Long proyectoId,

            @Parameter(description = "Nombre original del archivo", required = true)
            @RequestParam String nombreOriginal
    ) {
        log.info("GET /api/v1/archivos/versiones/{} - archivo: {}", proyectoId, nombreOriginal);
        return ResponseEntity.ok(archivoService.obtenerVersiones(proyectoId, nombreOriginal));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ARCHIVO_ELIMINAR', 'ADMIN')")
    @Operation(
            summary = "Eliminar archivo",
            description = "Elimina un archivo (soft delete)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Archivo eliminado"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Void> eliminarArchivo(
            @Parameter(description = "ID del archivo", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/archivos/{}", id);
        archivoService.eliminarArchivo(id);
        return ResponseEntity.noContent().build();
    }
}
