package com.inmobiliaria.terrenos.application.dto.venta;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para actualizar una venta.
 * Permite modificar datos descriptivos y financieros sin cambiar el terreno.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVentaRequest {

    private Long terrenoId;
    private Long apartadoId;

    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    private String compradorNombre;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String compradorEmail;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String compradorTelefono;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String compradorDireccion;

    @Size(max = 20, message = "El RFC no puede exceder 20 caracteres")
    private String compradorRfc;

    @Size(max = 20, message = "El CURP no puede exceder 20 caracteres")
    private String compradorCurp;

    private BigDecimal precioTotal;
    private BigDecimal montoApartadoAcreditado;
    private BigDecimal montoFinal;
    private BigDecimal porcentajeComision;
    private BigDecimal montoComision;

    @Size(max = 50, message = "La forma de pago no puede exceder 50 caracteres")
    private String formaPago;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;
}
