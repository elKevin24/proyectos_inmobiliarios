package com.inmobiliaria.terrenos.application.dto.pago;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaPagoResponse {

    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    private String externalReference;
}
