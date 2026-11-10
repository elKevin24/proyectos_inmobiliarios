package com.inmobiliaria.terrenos.application.port.out;

import com.inmobiliaria.terrenos.application.dto.pago.CrearPreferenciaPagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PreferenciaPagoResponse;
import com.inmobiliaria.terrenos.application.dto.pago.WebhookMercadoPagoNotification;

/**
 * Puerto de salida para desacoplar pasarelas de pago (DIP & OCP).
 */
public interface PaymentGatewayPort {
    PreferenciaPagoResponse crearPreferencia(CrearPreferenciaPagoRequest request);
    void procesarNotificacionWebhook(WebhookMercadoPagoNotification notification, String paymentId, String externalReference);
}
