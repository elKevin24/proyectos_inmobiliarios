package com.inmobiliaria.terrenos.application.dto.venta;

import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para ventas
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponse {

    private Long id;
    private Long tenantId;
    private Long terrenoId;
    private String terrenoNumeroLote;
    private String terrenoManzana;
    private Long proyectoId;
    private String proyectoNombre;
    private Long apartadoId;
    private Long usuarioId;
    private String usuarioNombre;
    private String compradorNombre;
    private String compradorEmail;
    private String compradorTelefono;
    private String compradorDireccion;
    private String compradorRfc;
    private String compradorCurp;
    private LocalDate fechaVenta;
    private BigDecimal precioTotal;
    private BigDecimal montoApartadoAcreditado;
    private BigDecimal montoFinal;
    private BigDecimal porcentajeComision;
    private BigDecimal montoComision;
    private String formaPago;
    private EstadoVenta estado;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Calcula el monto neto (sin comisión)
     */
    public BigDecimal getMontoNeto() {
        if (montoFinal == null || montoComision == null) {
            return montoFinal;
        }
        return montoFinal.subtract(montoComision);
    }

    /**
     * Calcula el porcentaje pagado con apartado
     */
    public BigDecimal getPorcentajePagadoConApartado() {
        if (precioTotal == null || precioTotal.compareTo(BigDecimal.ZERO) == 0 ||
            montoApartadoAcreditado == null) {
            return BigDecimal.ZERO;
        }
        return montoApartadoAcreditado.multiply(BigDecimal.valueOf(100))
                .divide(precioTotal, 2, RoundingMode.HALF_UP);
    }

    /**
     * Verifica si la venta está completada y pagada
     */
    public boolean isPagada() {
        return estado == EstadoVenta.PAGADO;
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
