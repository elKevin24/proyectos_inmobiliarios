package com.inmobiliaria.terrenos.application.dto.pago;

import com.inmobiliaria.terrenos.domain.enums.MetodoPago;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registrar un nuevo pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePagoRequest {

    @NotNull(message = "El ID del plan de pago es obligatorio")
    private Long planPagoId;

    // Opcional: especificar la amortización a la que se aplica
    // Si no se especifica, se aplica a las cuotas más antiguas pendientes
    private Long amortizacionId;

    @NotNull(message = "La fecha de pago es obligatoria")
    @PastOrPresent(message = "La fecha de pago no puede ser en el futuro")
    private LocalDate fechaPago;

    @NotNull(message = "El monto pagado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto pagado debe ser mayor a 0")
    @Digits(integer = 13, fraction = 2, message = "El monto pagado debe tener máximo 13 dígitos enteros y 2 decimales")
    private BigDecimal montoPagado;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @Size(max = 100, message = "La referencia de pago no puede exceder 100 caracteres")
    private String referenciaPago;

    @Size(max = 5000, message = "Las observaciones no pueden exceder 5000 caracteres")
    private String observaciones;

    // Opcional: ruta del comprobante de pago
    @Size(max = 500, message = "La ruta del comprobante no puede exceder 500 caracteres")
    private String comprobanteRuta;

    /**
     * Validación: si el método de pago es TRANSFERENCIA o CHEQUE, debe haber referencia
     */
    @AssertTrue(message = "Para transferencias y cheques es obligatorio proporcionar una referencia de pago")
    public boolean isReferenciaRequerida() {
        if (metodoPago == null) {
            return true;
        }
        if (metodoPago == MetodoPago.TRANSFERENCIA || metodoPago == MetodoPago.CHEQUE) {
            return referenciaPago != null && !referenciaPago.trim().isEmpty();
        }
        return true;
    }
}
