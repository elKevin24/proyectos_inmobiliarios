package com.inmobiliaria.terrenos.application.dto.pago;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookMercadoPagoNotification {

    private String action;

    @JsonProperty("api_version")
    private String apiVersion;

    private Map<String, Object> data;

    @JsonProperty("date_created")
    private String dateCreated;

    private Long id;

    @JsonProperty("live_mode")
    private Boolean liveMode;

    private String type;

    @JsonProperty("user_id")
    private String userId;
}
