package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.pago.CreatePlanPagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PlanPagoResponse;
import com.inmobiliaria.terrenos.application.dto.pago.UpdatePlanPagoRequest;
import com.inmobiliaria.terrenos.domain.entity.PlanPago;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad PlanPago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlanPagoMapper {

    /**
     * Convierte CreatePlanPagoRequest a entidad PlanPago
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "montoFinanciado", ignore = true) // Se calcula automáticamente en @PrePersist
    @Mapping(target = "tasaInteresMensual", ignore = true) // Se calcula automáticamente en @PrePersist
    @Mapping(target = "fechaUltimoPago", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    PlanPago toEntity(CreatePlanPagoRequest request);

    /**
     * Convierte entidad PlanPago a PlanPagoResponse
     */
    @Mapping(target = "tipoPlanDescripcion", expression = "java(planPago.getTipoPlan() != null ? planPago.getTipoPlan().getDescripcion() : null)")
    @Mapping(target = "frecuenciaPagoDescripcion", expression = "java(planPago.getFrecuenciaPago() != null ? planPago.getFrecuenciaPago().getDescripcion() : null)")
    @Mapping(target = "totalAmortizaciones", ignore = true)
    @Mapping(target = "amortizacionesPagadas", ignore = true)
    @Mapping(target = "amortizacionesPendientes", ignore = true)
    @Mapping(target = "amortizacionesVencidas", ignore = true)
    @Mapping(target = "totalPagado", ignore = true)
    @Mapping(target = "totalPendiente", ignore = true)
    @Mapping(target = "porcentajeAvance", ignore = true)
    PlanPagoResponse toResponse(PlanPago planPago);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    List<PlanPagoResponse> toResponseList(List<PlanPago> planesPago);

    /**
     * Actualiza una entidad PlanPago existente con datos de UpdatePlanPagoRequest
     * Solo actualiza los campos no nulos del request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "ventaId", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "tipoPlan", ignore = true)
    @Mapping(target = "frecuenciaPago", ignore = true)
    @Mapping(target = "montoTotal", ignore = true)
    @Mapping(target = "enganche", ignore = true)
    @Mapping(target = "montoFinanciado", ignore = true)
    @Mapping(target = "tasaInteresAnual", ignore = true)
    @Mapping(target = "tasaInteresMensual", ignore = true)
    @Mapping(target = "aplicaInteres", ignore = true)
    @Mapping(target = "numeroPagos", ignore = true)
    @Mapping(target = "plazoMeses", ignore = true)
    @Mapping(target = "fechaInicio", ignore = true)
    @Mapping(target = "fechaPrimerPago", ignore = true)
    @Mapping(target = "fechaUltimoPago", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(UpdatePlanPagoRequest request, @MappingTarget PlanPago planPago);
}
