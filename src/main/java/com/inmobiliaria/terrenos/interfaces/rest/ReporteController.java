package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.reporte.DashboardResponse;
import com.inmobiliaria.terrenos.application.dto.reporte.ProyectoEstadisticasResponse;
import com.inmobiliaria.terrenos.application.service.ReporteService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para reportes y dashboard
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reportes", description = "Reportes, estadísticas y dashboard")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('REPORTE_VER', 'ADMIN')")
    @Operation(
            summary = "Dashboard principal",
            description = "Obtiene las estadísticas generales del negocio (proyectos, terrenos, ventas, comisiones)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DashboardResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<DashboardResponse> obtenerDashboard() {
        log.info("GET /api/v1/reportes/dashboard");
        return ResponseEntity.ok(reporteService.obtenerDashboard());
    }

    @GetMapping("/proyectos")
    @PreAuthorize("hasAnyAuthority('REPORTE_VER', 'ADMIN')")
    @Operation(
            summary = "Estadísticas por proyecto",
            description = "Obtiene estadísticas detalladas de todos los proyectos"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProyectoEstadisticasResponse.class))
                    )
            )
    })
    public ResponseEntity<List<ProyectoEstadisticasResponse>> obtenerEstadisticasPorProyecto() {
        log.info("GET /api/v1/reportes/proyectos");
        return ResponseEntity.ok(reporteService.obtenerEstadisticasPorProyecto());
    }

    @GetMapping("/proyectos/{id}")
    @PreAuthorize("hasAnyAuthority('REPORTE_VER', 'ADMIN')")
    @Operation(
            summary = "Estadísticas de un proyecto",
            description = "Obtiene estadísticas detalladas de un proyecto específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estadísticas obtenidas",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProyectoEstadisticasResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    public ResponseEntity<ProyectoEstadisticasResponse> obtenerEstadisticasProyecto(
            @Parameter(description = "ID del proyecto", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/reportes/proyectos/{}", id);
        return ResponseEntity.ok(reporteService.obtenerEstadisticasProyecto(id));
    }
}
