package com.inmobiliaria.terrenos.application.dto.terreno;

import com.inmobiliaria.terrenos.application.dto.plano.CoordenadasPlano;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para actualizar un terreno existente
 * Todos los campos son opcionales
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTerrenoRequest {

    @Size(max = 50, message = "El número de lote no puede exceder 50 caracteres")
    private String numeroLote;

    @Size(max = 50, message = "La manzana no puede exceder 50 caracteres")
    private String manzana;

    @DecimalMin(value = "0.0", inclusive = false, message = "El área debe ser mayor a 0")
    private BigDecimal area;

    @DecimalMin(value = "0.0", inclusive = false, message = "El frente debe ser mayor a 0")
    private BigDecimal frente;

    @DecimalMin(value = "0.0", inclusive = false, message = "El fondo debe ser mayor a 0")
    private BigDecimal fondo;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;

    @DecimalMin(value = "0.0", message = "El ajuste de precio no puede ser negativo")
    private BigDecimal precioAjuste;

    @DecimalMin(value = "0.0", message = "El multiplicador no puede ser negativo")
    private BigDecimal precioMultiplicador;

    private BigDecimal precioFinal;

    @Size(max = 500, message = "Las características no pueden exceder 500 caracteres")
    private String caracteristicas;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    // Coordenadas en el plano para renderizado interactivo
    private CoordenadasPlano coordenadasPlano;
}
