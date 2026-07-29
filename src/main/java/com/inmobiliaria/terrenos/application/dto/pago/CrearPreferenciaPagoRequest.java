package com.inmobiliaria.terrenos.application.dto.pago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearPreferenciaPagoRequest {

    @NotNull(message = "El planPagoId es obligatorio")
    private Long planPagoId;

    private Long amortizacionId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    private String titulo;
    private String backUrlSuccess;
    private String backUrlFailure;
    private String backUrlPending;
}
