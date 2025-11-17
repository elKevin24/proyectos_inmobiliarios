package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.ProyectoResponse;
import com.inmobiliaria.terrenos.application.dto.proyecto.UpdateProyectoRequest;
import com.inmobiliaria.terrenos.application.service.ProyectoService;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de proyectos inmobiliarios
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/proyectos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Proyectos", description = "Gestión de proyectos inmobiliarios")
@SecurityRequirement(name = "bearerAuth")
public class ProyectoController {

    private final ProyectoService proyectoService;

    /**
     * Lista todos los proyectos del tenant
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROYECTO_VER', 'ADMIN')")
    @Operation(
            summary = "Listar proyectos",
            description = "Obtiene la lista de todos los proyectos del tenant actual"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de proyectos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProyectoResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<ProyectoResponse>> listarProyectos(
            @Parameter(description = "Filtrar por estado del proyecto")
            @RequestParam(required = false) EstadoProyecto estado,

            @Parameter(description = "Solo proyectos con terrenos disponibles")
            @RequestParam(required = false, defaultValue = "false") Boolean disponibles,

            @Parameter(description = "Solo proyectos activos (en venta)")
            @RequestParam(required = false, defaultValue = "false") Boolean activos
    ) {
        log.info("GET /api/v1/proyectos - estado: {}, disponibles: {}, activos: {}", estado, disponibles, activos);

        List<ProyectoResponse> proyectos;

        if (activos) {
            proyectos = proyectoService.listarProyectosActivos();
        } else if (disponibles) {
            proyectos = proyectoService.listarProyectosConTerrenosDisponibles();
        } else if (estado != null) {
            proyectos = proyectoService.listarProyectosPorEstado(estado);
        } else {
            proyectos = proyectoService.listarProyectos();
        }

        return ResponseEntity.ok(proyectos);
    }

    /**
     * Obtiene un proyecto por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROYECTO_VER', 'ADMIN')")
    @Operation(
            summary = "Obtener proyecto por ID",
            description = "Obtiene los detalles de un proyecto específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proyecto encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProyectoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ProyectoResponse> obtenerProyecto(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/proyectos/{}", id);
        ProyectoResponse proyecto = proyectoService.obtenerProyecto(id);
        return ResponseEntity.ok(proyecto);
    }

    /**
     * Crea un nuevo proyecto
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROYECTO_CREAR', 'ADMIN')")
    @Operation(
            summary = "Crear proyecto",
            description = "Crea un nuevo proyecto inmobiliario"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Proyecto creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProyectoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un proyecto con ese nombre"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ProyectoResponse> crearProyecto(
            @Valid @RequestBody CreateProyectoRequest request
    ) {
        log.info("POST /api/v1/proyectos - Nombre: {}", request.getNombre());
        ProyectoResponse proyecto = proyectoService.crearProyecto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(proyecto);
    }

    /**
     * Actualiza un proyecto existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROYECTO_EDITAR', 'ADMIN')")
    @Operation(
            summary = "Actualizar proyecto",
            description = "Actualiza los datos de un proyecto existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Proyecto actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProyectoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un proyecto con ese nombre"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ProyectoResponse> actualizarProyecto(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateProyectoRequest request
    ) {
        log.info("PUT /api/v1/proyectos/{}", id);
        ProyectoResponse proyecto = proyectoService.actualizarProyecto(id, request);
        return ResponseEntity.ok(proyecto);
    }

    /**
     * Elimina un proyecto (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROYECTO_ELIMINAR', 'ADMIN')")
    @Operation(
            summary = "Eliminar proyecto",
            description = "Elimina un proyecto (soft delete). No se puede eliminar si tiene terrenos vendidos o apartados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proyecto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar por tener terrenos vendidos/apartados"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Void> eliminarProyecto(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/proyectos/{}", id);
        proyectoService.eliminarProyecto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambia el estado de un proyecto
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('PROYECTO_EDITAR', 'ADMIN')")
    @Operation(
            summary = "Cambiar estado del proyecto",
            description = "Cambia el estado de un proyecto (PLANIFICACION, EN_VENTA, VENDIDO, FINALIZADO, CANCELADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado cambiado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProyectoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ProyectoResponse> cambiarEstado(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable Long id,

            @Parameter(description = "Nuevo estado del proyecto", required = true)
            @RequestParam EstadoProyecto estado
    ) {
        log.info("PATCH /api/v1/proyectos/{}/estado - Nuevo estado: {}", id, estado);
        ProyectoResponse proyecto = proyectoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(proyecto);
    }
}
