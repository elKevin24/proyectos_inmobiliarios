package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.pago.CrearPreferenciaPagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PreferenciaPagoResponse;
import com.inmobiliaria.terrenos.application.service.MercadoPagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para Pasarela de Pagos (Generación de Preferencias de MercadoPago/Stripe).
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/pasarela-pago")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pasarela de Pagos", description = "Endpoints para Checkout e Integración con Pasarelas de Pago")
public class PasarelaPagoController {

    private final MercadoPagoService mercadoPagoService;

    @PostMapping("/preferencia")
    @PreAuthorize("hasAuthority('PAGO_REGISTRAR')")
    @Operation(summary = "Generar Preferencia de Checkout", description = "Crea una preferencia de pago en MercadoPago para redirigir al cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferencia creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<PreferenciaPagoResponse> crearPreferencia(@Valid @RequestBody CrearPreferenciaPagoRequest request) {
        log.info("POST /api/v1/pasarela-pago/preferencia - Plan: {}", request.getPlanPagoId());
        PreferenciaPagoResponse response = mercadoPagoService.crearPreferencia(request);
        return ResponseEntity.ok(response);
    }
}
