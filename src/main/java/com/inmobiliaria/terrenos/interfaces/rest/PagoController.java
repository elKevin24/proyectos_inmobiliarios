package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.pago.CreatePagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PagoResponse;
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

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagos", description = "Registro y gestión de pagos")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PAGO_REGISTRAR', 'ADMIN')")
    @Operation(summary = "Registrar pago", description = "Registra un nuevo pago y lo aplica automáticamente a las cuotas correspondientes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o no hay cuotas pendientes"),
            @ApiResponse(responseCode = "404", description = "Plan de pago no encontrado")
    })
    public ResponseEntity<PagoResponse> registrarPago(@Valid @RequestBody CreatePagoRequest request) {
        log.info("POST /api/v1/pagos - Plan: {}, Monto: {}, Método: {}",
                request.getPlanPagoId(), request.getMontoPagado(), request.getMetodoPago());
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.aplicarPago(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAGO_REGISTRAR', 'ADMIN')")
    @Operation(summary = "Obtener pago por ID", description = "Obtiene el detalle de un pago específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PagoResponse> obtenerPago(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id) {
        log.info("GET /api/v1/pagos/{}", id);
        return ResponseEntity.ok(pagoService.obtenerPago(id));
    }

    @GetMapping("/venta/{ventaId}")
    @PreAuthorize("hasAnyAuthority('PAGO_REGISTRAR', 'VENTA_VER', 'ADMIN')")
    @Operation(summary = "Listar pagos por venta", description = "Obtiene todos los pagos asociados a una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pagos obtenida",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Venta o plan de pago no encontrado")
    })
    public ResponseEntity<List<PagoResponse>> listarPagosPorVenta(
            @Parameter(description = "ID de la venta", required = true) @PathVariable Long ventaId) {
        log.info("GET /api/v1/pagos/venta/{}", ventaId);
        return ResponseEntity.ok(pagoService.listarPagosPorVenta(ventaId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAGO_REGISTRAR', 'ADMIN')")
    @Operation(summary = "Eliminar pago", description = "Elimina un pago (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pago eliminado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<Void> eliminarPago(
            @Parameter(description = "ID del pago", required = true) @PathVariable Long id) {
        log.info("DELETE /api/v1/pagos/{}", id);
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }
}
