package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.pago.CreatePagoRequest;
import com.inmobiliaria.terrenos.application.dto.pago.PagoResponse;
import com.inmobiliaria.terrenos.domain.entity.Pago;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Pago
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PagoMapper {

    /**
     * Convierte CreatePagoRequest a entidad Pago
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    @Mapping(target = "montoACapital", ignore = true) // Se calcula en el servicio
    @Mapping(target = "montoAInteres", ignore = true) // Se calcula en el servicio
    @Mapping(target = "montoAMora", ignore = true) // Se calcula en el servicio
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Pago toEntity(CreatePagoRequest request);

    /**
     * Convierte entidad Pago a PagoResponse
     */
    @Mapping(target = "metodoPagoDescripcion", expression = "java(pago.getMetodoPago() != null ? pago.getMetodoPago().getDescripcion() : null)")
    @Mapping(target = "estadoDescripcion", expression = "java(pago.getEstado() != null ? pago.getEstado().getDescripcion() : null)")
    @Mapping(target = "usuarioNombre", ignore = true)
    PagoResponse toResponse(Pago pago);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    List<PagoResponse> toResponseList(List<Pago> pagos);
}
