package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.pago.CreatePagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PagoResponse;
import com.inmobiliaria.terrenos.application.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
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
 * Controlador REST para registro de pagos
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagos", description = "Registro y gestión de pagos")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService pagoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PAGO_VER', 'ADMIN')")
    @Operation(summary = "Listar todos los pagos", description = "Obtiene la lista de todos los pagos registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class)))
    })
    public ResponseEntity<List<PagoResponse>> listarPagos() {
        log.info("GET /api/v1/pagos");
        return ResponseEntity.ok(pagoService.listarPagos());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PAGO_REGISTRAR', 'ADMIN')")
    @Operation(summary = "Registrar pago", description = "Registra un nuevo pago y lo aplica automáticamente a las cuotas correspondientes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pago registrado y aplicado exitosamente",
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
    @PreAuthorize("hasAnyAuthority('PAGO_VER', 'ADMIN')")
    @Operation(summary = "Obtener pago por ID", description = "Obtiene el detalle de un pago específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PagoResponse> obtenerPago(@PathVariable Long id) {
        log.info("GET /api/v1/pagos/{}", id);
        return ResponseEntity.ok(pagoService.obtenerPago(id));
    }

    @GetMapping("/venta/{ventaId}")
    @PreAuthorize("hasAnyAuthority('PAGO_VER', 'ADMIN')")
    @Operation(summary = "Listar pagos por venta", description = "Obtiene todos los pagos asociados a la venta indicada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagos obtenidos",
                    content = @Content(schema = @Schema(implementation = PagoResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró plan de pago para esta venta")
    })
    public ResponseEntity<List<PagoResponse>> listarPagosPorVenta(@PathVariable Long ventaId) {
        log.info("GET /api/v1/pagos/venta/{}", ventaId);
        return ResponseEntity.ok(pagoService.listarPagosPorVenta(ventaId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PAGO_ELIMINAR', 'ADMIN')")
    @Operation(summary = "Eliminar pago", description = "Elimina lógicamente un pago")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pago eliminado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "409", description = "El pago no puede eliminarse en su estado actual")
    })
    public ResponseEntity<Void> eliminarPago(@PathVariable Long id) {
        log.info("DELETE /api/v1/pagos/{}", id);
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }
}
