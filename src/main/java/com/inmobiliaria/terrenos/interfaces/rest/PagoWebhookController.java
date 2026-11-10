package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.pago.WebhookMercadoPagoNotification;
import com.inmobiliaria.terrenos.application.port.out.PaymentGatewayPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST público para recepción e ingesta de Webhooks de Pasarelas de Pago.
 * Depende de {@link PaymentGatewayPort} (DIP).
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 1.2.0
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Endpoints receptores de Webhooks en tiempo real")
public class PagoWebhookController {

    private final PaymentGatewayPort paymentGatewayPort;

    @PostMapping("/mercadopago")
    @Operation(summary = "Webhook de MercadoPago", description = "Endpoint público para recibir notificaciones instantáneas de pago")
    public ResponseEntity<String> recibirWebhookMercadoPago(
            @RequestBody(required = false) WebhookMercadoPagoNotification notification,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "external_reference", required = false) String externalReference) {

        log.info("POST /api/v1/webhooks/mercadopago - id: {}, topic: {}, external_ref: {}", id, topic, externalReference);

        if (notification == null) {
            notification = new WebhookMercadoPagoNotification();
        }

        String paymentId = id != null ? id : (notification.getData() != null && notification.getData().containsKey("id") ?
                notification.getData().get("id").toString() : "MP-TEST-" + System.currentTimeMillis());

        paymentGatewayPort.procesarNotificacionWebhook(notification, paymentId, externalReference);

        return ResponseEntity.ok("OK");
    }
}
