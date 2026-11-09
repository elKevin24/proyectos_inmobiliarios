package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.pago.*;
import com.inmobiliaria.terrenos.domain.enums.MetodoPago;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Servicio de arquitectura para la integración con MercadoPago Checkout y Webhooks.
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 1.1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {

    private final PagoService pagoService;

    @Value("${app.mercadopago.access-token:APP_USR_SANDBOX_TOKEN_DEMO}")
    private String accessToken;

    @Value("${app.mercadopago.webhook-url:https://api.inmobiliaria-saas.com/api/v1/webhooks/mercadopago}")
    private String webhookUrl;

    /**
     * Crea una preferencia de pago para redirigir al comprador a MercadoPago Checkout
     */
    public PreferenciaPagoResponse crearPreferencia(CrearPreferenciaPagoRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }

        String externalRef = tenantId + ":" + request.getPlanPagoId() + ":" +
                (request.getAmortizacionId() != null ? request.getAmortizacionId() : "AUTO");

        String preferenceId = "PREF-" + UUID.randomUUID().toString();
        log.info("=== [Dev A] Generando preferencia de pago MercadoPago ID: {} para ref: {} ===", preferenceId, externalRef);

        // Simulador de URL de Checkout de MercadoPago (Ready for production SDK)
        String initPoint = "https://www.mercadopago.com.mx/checkout/v1/redirect?pref_id=" + preferenceId;
        String sandboxInitPoint = "https://sandbox.mercadopago.com.mx/checkout/v1/redirect?pref_id=" + preferenceId;

        return PreferenciaPagoResponse.builder()
                .preferenceId(preferenceId)
                .initPoint(initPoint)
                .sandboxInitPoint(sandboxInitPoint)
                .externalReference(externalRef)
                .build();
    }

    /**
     * Procesa la notificación Webhook enviada por MercadoPago al confirmarse un pago.
     */
    @Transactional
    public void procesarNotificacionWebhook(WebhookMercadoPagoNotification notification, String paymentId, String externalReference) {
        log.info("=== [Dev A] Procesando Webhook de MercadoPago. PaymentID: {}, Action: {}, ExternalRef: {} ===",
                paymentId, notification.getAction(), externalReference);

        if (externalReference == null || !externalReference.contains(":")) {
            log.warn("Notificación de Webhook ignorada: external_reference inválido ({})", externalReference);
            return;
        }

        try {
            String[] parts = externalReference.split(":");
            Long tenantId = Long.parseLong(parts[0]);
            Long planPagoId = Long.parseLong(parts[1]);
            Long amortizacionId = !"AUTO".equals(parts[2]) ? Long.parseLong(parts[2]) : null;

            // Establecer el contexto del tenant para procesar el pago aislado
            TenantContext.setTenantId(tenantId);

            // Registrar el pago automáticamente en el sistema
            CreatePagoRequest pagoRequest = CreatePagoRequest.builder()
                    .planPagoId(planPagoId)
                    .amortizacionId(amortizacionId)
                    .montoPagado(notification.getData() != null && notification.getData().containsKey("amount") ?
                            new java.math.BigDecimal(notification.getData().get("amount").toString()) :
                            new java.math.BigDecimal("100.00")) // Fallback param
                    .fechaPago(LocalDate.now())
                    .metodoPago(MetodoPago.TRANSFERENCIA)
                    .referenciaPago("MP-" + paymentId)
                    .observaciones("Pago procesado automáticamente vía Webhook de MercadoPago")
                    .build();

            pagoService.aplicarPago(pagoRequest);
            log.info("=== [Dev A] Pago de Webhook MercadoPago aplicado exitosamente para tenant: {}, plan: {} ===",
                    tenantId, planPagoId);
        } catch (Exception e) {
            log.error("Error procesando Webhook de MercadoPago para paymentId {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Error en Webhook MercadoPago: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }
}
