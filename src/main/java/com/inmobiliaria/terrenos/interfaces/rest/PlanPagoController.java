package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.pago.*;
import com.inmobiliaria.terrenos.application.service.PagoService;
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
 * Controlador REST para gestión de planes de pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/planes-pago")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Planes de Pago", description = "Gestión de planes de pago y financiamiento")
@SecurityRequirement(name = "bearerAuth")
public class PlanPagoController {

    private final PagoService pagoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PLAN_PAGO_VER', 'ADMIN')")
    @Operation(summary = "Listar planes de pago", description = "Obtiene la lista de todos los planes de pago")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PlanPagoResponse.class))))
    })
    public ResponseEntity<List<PlanPagoResponse>> listarPlanesPago() {
        log.info("GET /api/v1/planes-pago");
        return ResponseEntity.ok(pagoService.listarPlanesPago());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PLAN_PAGO_VER', 'ADMIN')")
    @Operation(summary = "Obtener plan de pago por ID", description = "Obtiene un plan de pago específico con sus estadísticas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan de pago encontrado",
                    content = @Content(schema = @Schema(implementation = PlanPagoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan de pago no encontrado")
    })
    public ResponseEntity<PlanPagoResponse> obtenerPlanPago(@PathVariable Long id) {
        log.info("GET /api/v1/planes-pago/{}", id);
        return ResponseEntity.ok(pagoService.obtenerPlanPago(id));
    }

    @GetMapping("/venta/{ventaId}")
    @PreAuthorize("hasAnyAuthority('PLAN_PAGO_VER', 'ADMIN')")
    @Operation(summary = "Obtener plan de pago por venta", description = "Obtiene el plan de pago asociado a una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan de pago encontrado",
                    content = @Content(schema = @Schema(implementation = PlanPagoResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró plan de pago para esta venta")
    })
    public ResponseEntity<PlanPagoResponse> obtenerPlanPagoPorVenta(@PathVariable Long ventaId) {
        log.info("GET /api/v1/planes-pago/venta/{}", ventaId);
        return ResponseEntity.ok(pagoService.obtenerPlanPagoPorVenta(ventaId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PLAN_PAGO_CREAR', 'ADMIN')")
    @Operation(summary = "Crear plan de pago", description = "Crea un nuevo plan de pago para una venta y genera la tabla de amortización")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan de pago creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PlanPagoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya existe un plan de pago para esta venta")
    })
    public ResponseEntity<PlanPagoResponse> crearPlanPago(@Valid @RequestBody CreatePlanPagoRequest request) {
        log.info("POST /api/v1/planes-pago - Venta: {}, Tipo: {}, Monto: {}",
                request.getVentaId(), request.getTipoPlan(), request.getMontoTotal());
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.crearPlanPago(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PLAN_PAGO_EDITAR', 'ADMIN')")
    @Operation(summary = "Actualizar plan de pago", description = "Actualiza ciertos campos de un plan de pago (solo campos permitidos)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan de pago actualizado",
                    content = @Content(schema = @Schema(implementation = PlanPagoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan de pago no encontrado")
    })
    public ResponseEntity<PlanPagoResponse> actualizarPlanPago(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanPagoRequest request) {
        log.info("PUT /api/v1/planes-pago/{}", id);
        return ResponseEntity.ok(pagoService.actualizarPlanPago(id, request));
    }

    @GetMapping("/{id}/tabla-amortizacion")
    @PreAuthorize("hasAnyAuthority('PLAN_PAGO_VER', 'ADMIN')")
    @Operation(summary = "Obtener tabla de amortización", description = "Obtiene la tabla de amortización completa del plan de pago")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tabla de amortización obtenida",
                    content = @Content(schema = @Schema(implementation = TablaAmortizacionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan de pago no encontrado")
    })
    public ResponseEntity<TablaAmortizacionResponse> obtenerTablaAmortizacion(@PathVariable Long id) {
        log.info("GET /api/v1/planes-pago/{}/tabla-amortizacion", id);
        return ResponseEntity.ok(pagoService.obtenerTablaAmortizacion(id));
    }

    @GetMapping("/{id}/estado-cuenta")
    @PreAuthorize("hasAnyAuthority('PLAN_PAGO_VER', 'ADMIN')")
    @Operation(summary = "Obtener estado de cuenta", description = "Obtiene el estado de cuenta completo con historial de pagos y estadísticas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de cuenta obtenido",
                    content = @Content(schema = @Schema(implementation = EstadoCuentaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Plan de pago no encontrado")
    })
    public ResponseEntity<EstadoCuentaResponse> obtenerEstadoCuenta(@PathVariable Long id) {
        log.info("GET /api/v1/planes-pago/{}/estado-cuenta", id);
        return ResponseEntity.ok(pagoService.obtenerEstadoCuenta(id));
    }
}
