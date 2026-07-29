package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.pago.CrearPreferenciaPagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.CreatePagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PreferenciaPagoResponse;
import com.inmobiliaria.terrenos.application.dto.pago.WebhookMercadoPagoNotification;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MercadoPagoServiceTest {

    @Mock
    private PagoService pagoService;

    @InjectMocks
    private MercadoPagoService mercadoPagoService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Crear Preferencia de Pago Exitoso")
    void testCrearPreferencia_Exito() {
        CrearPreferenciaPagoRequest request = CrearPreferenciaPagoRequest.builder()
                .planPagoId(10L)
                .amortizacionId(100L)
                .monto(new BigDecimal("2500.00"))
                .titulo("Cuota 1 Terreno A-10")
                .build();

        PreferenciaPagoResponse response = mercadoPagoService.crearPreferencia(request);

        assertNotNull(response);
        assertNotNull(response.getPreferenceId());
        assertTrue(response.getInitPoint().contains(response.getPreferenceId()));
        assertEquals("1:10:100", response.getExternalReference());
    }

    @Test
    @DisplayName("Procesar Webhook Notificacion MercadoPago Exitoso")
    void testProcesarNotificacionWebhook_Exito() {
        WebhookMercadoPagoNotification notification = WebhookMercadoPagoNotification.builder()
                .action("payment.created")
                .data(Map.of("id", "MP-PAYMENT-999", "amount", "2500.00"))
                .build();

        String paymentId = "MP-PAYMENT-999";
        String externalReference = "1:10:100";

        mercadoPagoService.procesarNotificacionWebhook(notification, paymentId, externalReference);

        verify(pagoService, times(1)).aplicarPago(any(CreatePagoRequest.class));
    }
}
