package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.auditoria.*;
import com.inmobiliaria.terrenos.application.service.AuditService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para auditoría
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auditoría", description = "Consulta de logs de auditoría (solo administradores)")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaController {

    private final AuditService auditService;

    @GetMapping("/simple")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener logs de auditoría simple", description = "Obtiene logs de acciones generales (logins, exports, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs obtenidos",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuditLogSimpleResponse.class))))
    })
    public ResponseEntity<List<AuditLogSimpleResponse>> obtenerLogsSimples(
            @Parameter(description = "ID del usuario")
            @RequestParam(required = false) Long usuarioId,

            @Parameter(description = "Acción a filtrar")
            @RequestParam(required = false) String accion,

            @Parameter(description = "Fecha de inicio")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,

            @Parameter(description = "Fecha de fin")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,

            @Parameter(description = "Límite de resultados")
            @RequestParam(required = false, defaultValue = "100") Integer limit
    ) {
        log.info("GET /api/v1/auditoria/simple - usuario: {}, accion: {}, limit: {}", usuarioId, accion, limit);

        AuditFiltrosRequest filtros = AuditFiltrosRequest.builder()
                .usuarioId(usuarioId)
                .accion(accion)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .limit(limit)
                .build();

        return ResponseEntity.ok(auditService.obtenerLogsSimples(filtros));
    }

    @GetMapping("/critica")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener logs de auditoría crítica", description = "Obtiene logs de cambios en datos importantes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs obtenidos",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuditLogCriticaResponse.class))))
    })
    public ResponseEntity<List<AuditLogCriticaResponse>> obtenerLogsCriticos(
            @Parameter(description = "ID del usuario")
            @RequestParam(required = false) Long usuarioId,

            @Parameter(description = "Tabla a filtrar")
            @RequestParam(required = false) String tabla,

            @Parameter(description = "Fecha de inicio")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,

            @Parameter(description = "Fecha de fin")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,

            @Parameter(description = "Límite de resultados")
            @RequestParam(required = false, defaultValue = "100") Integer limit
    ) {
        log.info("GET /api/v1/auditoria/critica - usuario: {}, tabla: {}, limit: {}", usuarioId, tabla, limit);

        AuditFiltrosRequest filtros = AuditFiltrosRequest.builder()
                .usuarioId(usuarioId)
                .tabla(tabla)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .limit(limit)
                .build();

        return ResponseEntity.ok(auditService.obtenerLogsCriticos(filtros));
    }

    @GetMapping("/registro/{tabla}/{registroId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener historial de un registro", description = "Obtiene todos los cambios de un registro específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuditLogCriticaResponse.class))))
    })
    public ResponseEntity<List<AuditLogCriticaResponse>> obtenerHistorialRegistro(
            @PathVariable String tabla,
            @PathVariable Long registroId
    ) {
        log.info("GET /api/v1/auditoria/registro/{}/{}", tabla, registroId);
        return ResponseEntity.ok(auditService.obtenerHistorialRegistro(tabla, registroId));
    }

    @GetMapping("/campo/{tabla}/{registroId}/{campo}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener historial de un campo", description = "Obtiene todos los cambios de un campo específico de un registro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuditLogCriticaResponse.class))))
    })
    public ResponseEntity<List<AuditLogCriticaResponse>> obtenerHistorialCampo(
            @PathVariable String tabla,
            @PathVariable Long registroId,
            @PathVariable String campo
    ) {
        log.info("GET /api/v1/auditoria/campo/{}/{}/{}", tabla, registroId, campo);
        return ResponseEntity.ok(auditService.obtenerHistorialCampo(tabla, registroId, campo));
    }

    @GetMapping("/logins/{usuarioId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Obtener últimos logins de un usuario", description = "Obtiene los últimos inicios de sesión de un usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logins obtenidos",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuditLogSimpleResponse.class))))
    })
    public ResponseEntity<List<AuditLogSimpleResponse>> obtenerUltimosLogins(
            @PathVariable Long usuarioId,
            @Parameter(description = "Límite de resultados")
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        log.info("GET /api/v1/auditoria/logins/{} - limit: {}", usuarioId, limit);
        return ResponseEntity.ok(auditService.obtenerUltimosLogins(usuarioId, limit));
    }

    @PostMapping("/archivar")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Archivar logs antiguos", description = "Archiva logs con más de 1 año y los elimina de las tablas principales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs archivados",
                    content = @Content(schema = @Schema(implementation = Integer.class)))
    })
    public ResponseEntity<Integer> archivarLogsAntiguos() {
        log.info("POST /api/v1/auditoria/archivar - Iniciando proceso de archivado");
        int totalArchivados = auditService.archivarLogsAntiguos();
        log.info("Proceso de archivado completado: {} logs archivados", totalArchivados);
        return ResponseEntity.ok(totalArchivados);
    }
}
