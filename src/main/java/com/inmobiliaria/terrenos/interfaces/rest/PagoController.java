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
}
