package com.inmobiliaria.terrenos.application.dto.apartado;

import com.inmobiliaria.terrenos.domain.enums.EstadoApartado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para apartados
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApartadoResponse {

    private Long id;
    private Long tenantId;
    private Long terrenoId;
    private String terrenoNumeroLote;
    private String terrenoManzana;
    private Long proyectoId;
    private String proyectoNombre;
    private Long cotizacionId;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteDireccion;
    private BigDecimal montoApartado;
    private BigDecimal precioTotal;
    private LocalDate fechaApartado;
    private Integer duracionDias;
    private LocalDate fechaVencimiento;
    private EstadoApartado estado;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Calcula el porcentaje de apartado
     */
    public BigDecimal getPorcentajeApartado() {
        if (precioTotal == null || precioTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return montoApartado.multiply(BigDecimal.valueOf(100))
                .divide(precioTotal, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el saldo pendiente
     */
    public BigDecimal getSaldoPendiente() {
        if (precioTotal == null || montoApartado == null) {
            return BigDecimal.ZERO;
        }
        return precioTotal.subtract(montoApartado);
    }

    /**
     * Verifica si el apartado está vigente
     */
    public boolean isVigente() {
        return estado == EstadoApartado.VIGENTE &&
               fechaVencimiento != null &&
               !fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Verifica si el apartado está vencido
     */
    public boolean isVencido() {
        return fechaVencimiento != null &&
               fechaVencimiento.isBefore(LocalDate.now()) &&
               estado == EstadoApartado.VIGENTE;
    }

    /**
     * Obtiene identificador del terreno
     */
    public String getTerrenoIdentificador() {
        if (terrenoManzana != null && !terrenoManzana.isBlank()) {
            return String.format("Mz %s Lt %s", terrenoManzana, terrenoNumeroLote);
        }
        return String.format("Lt %s", terrenoNumeroLote);
    }
}
