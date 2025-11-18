package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.cotizacion.CotizacionResponse;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CreateCotizacionRequest;
import com.inmobiliaria.terrenos.application.service.CotizacionService;
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
 * Controlador REST para gestión de cotizaciones
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/cotizaciones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cotizaciones", description = "Gestión de cotizaciones de terrenos")
@SecurityRequirement(name = "bearerAuth")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('COTIZACION_VER', 'ADMIN')")
    @Operation(summary = "Listar cotizaciones", description = "Obtiene la lista de cotizaciones con filtros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CotizacionResponse.class))))
    })
    public ResponseEntity<List<CotizacionResponse>> listarCotizaciones(
            @Parameter(description = "Solo cotizaciones vigentes")
            @RequestParam(required = false, defaultValue = "false") Boolean vigentes,

            @Parameter(description = "Buscar por nombre de cliente")
            @RequestParam(required = false) String cliente
    ) {
        log.info("GET /api/v1/cotizaciones - vigentes: {}, cliente: {}", vigentes, cliente);

        List<CotizacionResponse> cotizaciones;
        if (vigentes) {
            cotizaciones = cotizacionService.listarCotizacionesVigentes();
        } else if (cliente != null) {
            cotizaciones = cotizacionService.buscarPorCliente(cliente);
        } else {
            cotizaciones = cotizacionService.listarCotizaciones();
        }

        return ResponseEntity.ok(cotizaciones);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COTIZACION_VER', 'ADMIN')")
    @Operation(summary = "Obtener cotización por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cotización encontrada",
                    content = @Content(schema = @Schema(implementation = CotizacionResponse.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<CotizacionResponse> obtenerCotizacion(@PathVariable Long id) {
        log.info("GET /api/v1/cotizaciones/{}", id);
        return ResponseEntity.ok(cotizacionService.obtenerCotizacion(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('COTIZACION_CREAR', 'ADMIN')")
    @Operation(summary = "Crear cotización", description = "Crea una nueva cotización para un terreno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cotización creada",
                    content = @Content(schema = @Schema(implementation = CotizacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Terreno no encontrado")
    })
    public ResponseEntity<CotizacionResponse> crearCotizacion(@Valid @RequestBody CreateCotizacionRequest request) {
        log.info("POST /api/v1/cotizaciones - Terreno: {}, Cliente: {}", request.getTerrenoId(), request.getClienteNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.crearCotizacion(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('COTIZACION_ELIMINAR', 'ADMIN')")
    @Operation(summary = "Eliminar cotización")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cotización eliminada"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<Void> eliminarCotizacion(@PathVariable Long id) {
        log.info("DELETE /api/v1/cotizaciones/{}", id);
        cotizacionService.eliminarCotizacion(id);
        return ResponseEntity.noContent().build();
    }
}
