package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.cotizacion.CotizacionResponse;
import com.inmobiliaria.terrenos.application.dto.cotizacion.CreateCotizacionRequest;
import com.inmobiliaria.terrenos.domain.entity.Cotizacion;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Cotizacion
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CotizacionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "terreno", ignore = true)
    Cotizacion toEntity(CreateCotizacionRequest request);

    @Mapping(target = "terrenoNumeroLote", source = "terreno.numeroLote")
    @Mapping(target = "terrenoManzana", source = "terreno.manzana")
    @Mapping(target = "proyectoId", source = "terreno.proyectoId")
    @Mapping(target = "proyectoNombre", source = "terreno.proyecto.nombre")
    CotizacionResponse toResponse(Cotizacion cotizacion);

    List<CotizacionResponse> toResponseList(List<Cotizacion> cotizaciones);
}
