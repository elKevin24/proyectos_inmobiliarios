package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.pago.AmortizacionResponse;
import com.inmobiliaria.terrenos.domain.entity.Amortizacion;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Amortizacion
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AmortizacionMapper {

    /**
     * Convierte entidad Amortizacion a AmortizacionResponse
     */
    @Mapping(target = "estadoDescripcion", expression = "java(amortizacion.getEstado() != null ? amortizacion.getEstado().getDescripcion() : null)")
    AmortizacionResponse toResponse(Amortizacion amortizacion);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    List<AmortizacionResponse> toResponseList(List<Amortizacion> amortizaciones);
}
