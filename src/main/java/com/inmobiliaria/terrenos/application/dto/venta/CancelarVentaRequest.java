package com.inmobiliaria.terrenos.application.dto.venta;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cancelar una venta.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelarVentaRequest {

    @Size(max = 1000, message = "El motivo no puede exceder 1000 caracteres")
    private String motivo;
}
