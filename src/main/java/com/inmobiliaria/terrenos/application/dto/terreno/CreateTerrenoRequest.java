package com.inmobiliaria.terrenos.application.dto.terreno;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inmobiliaria.terrenos.application.dto.plano.CoordenadasPlano;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.shared.util.RawJsonDeserializer;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para crear un nuevo terreno
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTerrenoRequest {

    @NotNull(message = "El proyecto es obligatorio")
    private Long proyectoId;

    private Long faseId; // Opcional

    @JsonAlias("numero")
    @NotBlank(message = "El número de lote es obligatorio")
    @Size(max = 50, message = "El número de lote no puede exceder 50 caracteres")
    private String numeroLote;

    @Size(max = 50, message = "La manzana no puede exceder 50 caracteres")
    private String manzana;

    @NotNull(message = "El área es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "El área debe ser mayor a 0")
    private BigDecimal area;

    @DecimalMin(value = "0.0", inclusive = false, message = "El frente debe ser mayor a 0")
    private BigDecimal frente;

    @DecimalMin(value = "0.0", inclusive = false, message = "El fondo debe ser mayor a 0")
    private BigDecimal fondo;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;

    @JsonAlias("ajustePrecio")
    @DecimalMin(value = "0.0", message = "El ajuste de precio no puede ser negativo")
    private BigDecimal precioAjuste;

    @JsonAlias("multiplicadorPrecio")
    @DecimalMin(value = "0.0", message = "El multiplicador no puede ser negativo")
    private BigDecimal precioMultiplicador;

    private BigDecimal precioFinal;

    @JsonAlias("nombre")
    @JsonDeserialize(using = RawJsonDeserializer.class)
    @Size(max = 500, message = "Las características no pueden exceder 500 caracteres")
    private String caracteristicas;

    @JsonIgnore
    public String getNombre() {
        return caracteristicas;
    }

    public void setNombre(String nombre) {
        this.caracteristicas = nombre;
    }

    @JsonAlias("descripcion")
    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    private EstadoTerreno estado;

    // Coordenadas en el plano para renderizado interactivo
    private CoordenadasPlano coordenadasPlano;
}
