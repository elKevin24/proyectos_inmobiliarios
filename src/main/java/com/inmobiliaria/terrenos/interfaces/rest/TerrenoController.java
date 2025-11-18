package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.TerrenoResponse;
import com.inmobiliaria.terrenos.application.dto.terreno.UpdateTerrenoRequest;
import com.inmobiliaria.terrenos.application.service.TerrenoService;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
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
 * Controlador REST para gestión de terrenos/lotes
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/terrenos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Terrenos", description = "Gestión de terrenos/lotes de proyectos inmobiliarios")
@SecurityRequirement(name = "bearerAuth")
public class TerrenoController {

    private final TerrenoService terrenoService;

    /**
     * Lista todos los terrenos del tenant con filtros opcionales
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('TERRENO_VER', 'ADMIN')")
    @Operation(
            summary = "Listar terrenos",
            description = "Obtiene la lista de terrenos con filtros opcionales (proyecto, estado, disponibles)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de terrenos obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TerrenoResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<TerrenoResponse>> listarTerrenos(
            @Parameter(description = "Filtrar por proyecto")
            @RequestParam(required = false) Long proyectoId,

            @Parameter(description = "Filtrar por estado del terreno")
            @RequestParam(required = false) EstadoTerreno estado,

            @Parameter(description = "Solo terrenos disponibles")
            @RequestParam(required = false, defaultValue = "false") Boolean disponibles
    ) {
        log.info("GET /api/v1/terrenos - proyectoId: {}, estado: {}, disponibles: {}", proyectoId, estado, disponibles);

        List<TerrenoResponse> terrenos;

        if (proyectoId != null && disponibles) {
            terrenos = terrenoService.listarTerrenosDisponibles(proyectoId);
        } else if (proyectoId != null) {
            terrenos = terrenoService.listarTerrenosPorProyecto(proyectoId);
        } else if (estado != null) {
            terrenos = terrenoService.listarTerrenosPorEstado(estado);
        } else {
            terrenos = terrenoService.listarTerrenos();
        }

        return ResponseEntity.ok(terrenos);
    }

    /**
     * Obtiene un terreno por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TERRENO_VER', 'ADMIN')")
    @Operation(
            summary = "Obtener terreno por ID",
            description = "Obtiene los detalles de un terreno específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Terreno encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerrenoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Terreno no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<TerrenoResponse> obtenerTerreno(
            @Parameter(description = "ID del terreno", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/terrenos/{}", id);
        TerrenoResponse terreno = terrenoService.obtenerTerreno(id);
        return ResponseEntity.ok(terreno);
    }

    /**
     * Crea un nuevo terreno
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('TERRENO_CREAR', 'ADMIN')")
    @Operation(
            summary = "Crear terreno",
            description = "Crea un nuevo terreno/lote en un proyecto"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Terreno creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerrenoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe un terreno con ese número de lote"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<TerrenoResponse> crearTerreno(
            @Valid @RequestBody CreateTerrenoRequest request
    ) {
        log.info("POST /api/v1/terrenos - Lote: {}, Proyecto: {}", request.getNumeroLote(), request.getProyectoId());
        TerrenoResponse terreno = terrenoService.crearTerreno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(terreno);
    }

    /**
     * Actualiza un terreno existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TERRENO_EDITAR', 'ADMIN')")
    @Operation(
            summary = "Actualizar terreno",
            description = "Actualiza los datos de un terreno existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Terreno actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerrenoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Terreno no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe otro terreno con ese número de lote"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<TerrenoResponse> actualizarTerreno(
            @Parameter(description = "ID del terreno", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateTerrenoRequest request
    ) {
        log.info("PUT /api/v1/terrenos/{}", id);
        TerrenoResponse terreno = terrenoService.actualizarTerreno(id, request);
        return ResponseEntity.ok(terreno);
    }

    /**
     * Elimina un terreno (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TERRENO_ELIMINAR', 'ADMIN')")
    @Operation(
            summary = "Eliminar terreno",
            description = "Elimina un terreno (soft delete). No se puede eliminar si está vendido, en venta o apartado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Terreno eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Terreno no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar (vendido/apartado/en venta)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Void> eliminarTerreno(
            @Parameter(description = "ID del terreno", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/terrenos/{}", id);
        terrenoService.eliminarTerreno(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Cambia el estado de un terreno
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('TERRENO_EDITAR', 'ADMIN')")
    @Operation(
            summary = "Cambiar estado del terreno",
            description = "Cambia el estado de un terreno (DISPONIBLE, APARTADO, EN_VENTA, VENDIDO, RESERVADO)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado cambiado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TerrenoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
            @ApiResponse(responseCode = "404", description = "Terreno no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<TerrenoResponse> cambiarEstado(
            @Parameter(description = "ID del terreno", required = true)
            @PathVariable Long id,

            @Parameter(description = "Nuevo estado del terreno", required = true)
            @RequestParam EstadoTerreno estado
    ) {
        log.info("PATCH /api/v1/terrenos/{}/estado - Nuevo estado: {}", id, estado);
        TerrenoResponse terreno = terrenoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(terreno);
    }
}
