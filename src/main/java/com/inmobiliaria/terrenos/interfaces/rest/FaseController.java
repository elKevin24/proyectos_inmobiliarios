package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.fase.CreateFaseRequest;
import com.inmobiliaria.terrenos.application.dto.fase.FaseResponse;
import com.inmobiliaria.terrenos.application.dto.fase.UpdateFaseRequest;
import com.inmobiliaria.terrenos.application.service.FaseService;
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
 * Controlador REST para gestión de fases de proyectos
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/fases")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fases", description = "Gestión de fases/etapas de proyectos inmobiliarios")
@SecurityRequirement(name = "bearerAuth")
public class FaseController {

    private final FaseService faseService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PROYECTO_VER', 'ADMIN')")
    @Operation(summary = "Listar fases", description = "Obtiene la lista de fases con filtros opcionales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de fases obtenida",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FaseResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<FaseResponse>> listarFases(
            @Parameter(description = "Filtrar por proyecto")
            @RequestParam(required = false) Long proyectoId,

            @Parameter(description = "Solo fases activas")
            @RequestParam(required = false, defaultValue = "false") Boolean activas
    ) {
        log.info("GET /api/v1/fases - proyectoId: {}, activas: {}", proyectoId, activas);

        List<FaseResponse> fases;
        if (proyectoId != null && activas) {
            fases = faseService.listarFasesActivas(proyectoId);
        } else if (proyectoId != null) {
            fases = faseService.listarFasesPorProyecto(proyectoId);
        } else {
            fases = faseService.listarFases();
        }

        return ResponseEntity.ok(fases);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROYECTO_VER', 'ADMIN')")
    @Operation(summary = "Obtener fase por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fase encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fase no encontrada")
    })
    public ResponseEntity<FaseResponse> obtenerFase(@PathVariable Long id) {
        log.info("GET /api/v1/fases/{}", id);
        return ResponseEntity.ok(faseService.obtenerFase(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PROYECTO_CREAR', 'ADMIN')")
    @Operation(summary = "Crear fase", description = "Crea una nueva fase en un proyecto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fase creada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Fase duplicada")
    })
    public ResponseEntity<FaseResponse> crearFase(@Valid @RequestBody CreateFaseRequest request) {
        log.info("POST /api/v1/fases - Nombre: {}, Proyecto: {}", request.getNombre(), request.getProyectoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(faseService.crearFase(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROYECTO_EDITAR', 'ADMIN')")
    @Operation(summary = "Actualizar fase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fase actualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaseResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fase no encontrada")
    })
    public ResponseEntity<FaseResponse> actualizarFase(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFaseRequest request) {
        log.info("PUT /api/v1/fases/{}", id);
        return ResponseEntity.ok(faseService.actualizarFase(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PROYECTO_ELIMINAR', 'ADMIN')")
    @Operation(summary = "Eliminar fase")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Fase eliminada"),
            @ApiResponse(responseCode = "404", description = "Fase no encontrada"),
            @ApiResponse(responseCode = "409", description = "Fase tiene terrenos vendidos/apartados")
    })
    public ResponseEntity<Void> eliminarFase(@PathVariable Long id) {
        log.info("DELETE /api/v1/fases/{}", id);
        faseService.eliminarFase(id);
        return ResponseEntity.noContent().build();
    }
}
