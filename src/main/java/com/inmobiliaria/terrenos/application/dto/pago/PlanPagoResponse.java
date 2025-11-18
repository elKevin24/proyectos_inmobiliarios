package com.inmobiliaria.terrenos.application.dto.pago;

import com.inmobiliaria.terrenos.domain.enums.FrecuenciaPago;
import com.inmobiliaria.terrenos.domain.enums.TipoPlanPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para plan de pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanPagoResponse {

    private Long id;
    private Long ventaId;
    private Long clienteId;

    // Configuración del plan
    private TipoPlanPago tipoPlan;
    private String tipoPlanDescripcion;
    private FrecuenciaPago frecuenciaPago;
    private String frecuenciaPagoDescripcion;

    // Montos
    private BigDecimal montoTotal;
    private BigDecimal enganche;
    private BigDecimal montoFinanciado;

    // Intereses
    private BigDecimal tasaInteresAnual;
    private BigDecimal tasaInteresMensual;
    private Boolean aplicaInteres;

    // Plazos
    private Integer numeroPagos;
    private Integer plazoMeses;

    // Mora
    private BigDecimal tasaMoraMensual;
    private Integer diasGracia;

    // Fechas
    private LocalDate fechaInicio;
    private LocalDate fechaPrimerPago;
    private LocalDate fechaUltimoPago;

    // Estadísticas (calculadas)
    private Integer totalAmortizaciones;
    private Integer amortizacionesPagadas;
    private Integer amortizacionesPendientes;
    private Integer amortizacionesVencidas;
    private BigDecimal totalPagado;
    private BigDecimal totalPendiente;
    private BigDecimal porcentajeAvance;

    // Observaciones
    private String notas;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
