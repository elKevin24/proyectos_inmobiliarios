package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import com.inmobiliaria.terrenos.application.service.VentaService;
import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
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
 * Controlador REST para gestión de ventas
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ventas", description = "Gestión de ventas de terrenos")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VENTA_VER', 'ADMIN')")
    @Operation(summary = "Listar ventas", description = "Obtiene la lista de ventas con filtros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VentaResponse.class))))
    })
    public ResponseEntity<List<VentaResponse>> listarVentas(
            @Parameter(description = "Filtrar por estado de venta")
            @RequestParam(required = false) EstadoVenta estado
    ) {
        log.info("GET /api/v1/ventas - estado: {}", estado);

        List<VentaResponse> ventas;
        if (estado != null) {
            ventas = ventaService.listarVentasPorEstado(estado);
        } else {
            ventas = ventaService.listarVentas();
        }

        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENTA_VER', 'ADMIN')")
    @Operation(summary = "Obtener venta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta encontrada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<VentaResponse> obtenerVenta(@PathVariable Long id) {
        log.info("GET /api/v1/ventas/{}", id);
        return ResponseEntity.ok(ventaService.obtenerVenta(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('VENTA_CREAR', 'ADMIN')")
    @Operation(summary = "Crear venta",
               description = "Crea una nueva venta y cambia el estado del terreno a VENDIDO. " +
                           "Puede convertir un apartado existente en venta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venta creada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Terreno o apartado no encontrado"),
            @ApiResponse(responseCode = "409", description = "Terreno no disponible")
    })
    public ResponseEntity<VentaResponse> crearVenta(@Valid @RequestBody CreateVentaRequest request) {
        log.info("POST /api/v1/ventas - Terreno: {}, Comprador: {}", request.getTerrenoId(), request.getCompradorNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crearVenta(request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('VENTA_EDITAR', 'ADMIN')")
    @Operation(summary = "Cambiar estado de venta",
               description = "Cambia el estado de una venta (PENDIENTE, PAGADO, CANCELADO)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado cambiado",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<VentaResponse> cambiarEstado(
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado de la venta", required = true)
            @RequestParam EstadoVenta estado) {
        log.info("PATCH /api/v1/ventas/{}/estado - Nuevo estado: {}", id, estado);
        return ResponseEntity.ok(ventaService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VENTA_ELIMINAR', 'ADMIN')")
    @Operation(summary = "Eliminar venta", description = "Elimina una venta (solo canceladas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Venta eliminada"),
            @ApiResponse(responseCode = "404", description = "No encontrada"),
            @ApiResponse(responseCode = "409", description = "Solo se pueden eliminar ventas canceladas")
    })
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        log.info("DELETE /api/v1/ventas/{}", id);
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }
}
