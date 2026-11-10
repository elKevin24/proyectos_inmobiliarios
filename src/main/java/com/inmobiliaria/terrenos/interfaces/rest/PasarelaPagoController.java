package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.pago.CrearPreferenciaPagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PreferenciaPagoResponse;
import com.inmobiliaria.terrenos.application.port.out.PaymentGatewayPort;
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
 * Depende de la abstracción {@link PaymentGatewayPort} (DIP).
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 1.2.0
 */
@RestController
@RequestMapping("/api/v1/pasarela-pago")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pasarela de Pagos", description = "Endpoints para Checkout e Integración con Pasarelas de Pago")
public class PasarelaPagoController {

    private final PaymentGatewayPort paymentGatewayPort;

    @PostMapping("/preferencia")
    @PreAuthorize("hasAnyAuthority('PAGO_REGISTRAR', 'ADMIN')")
    @Operation(summary = "Generar Preferencia de Checkout", description = "Crea una preferencia de pago en la pasarela para redirigir al cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Preferencia creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<PreferenciaPagoResponse> crearPreferencia(@Valid @RequestBody CrearPreferenciaPagoRequest request) {
        log.info("POST /api/v1/pasarela-pago/preferencia - Plan: {}", request.getPlanPagoId());
        PreferenciaPagoResponse response = paymentGatewayPort.crearPreferencia(request);
        return ResponseEntity.ok(response);
    }
}
