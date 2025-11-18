package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.apartado.ApartadoResponse;
import com.inmobiliaria.terrenos.application.dto.apartado.CreateApartadoRequest;
import com.inmobiliaria.terrenos.domain.entity.Apartado;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Apartado
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ApartadoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "fechaApartado", ignore = true)
    @Mapping(target = "fechaVencimiento", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "terreno", ignore = true)
    @Mapping(target = "cotizacion", ignore = true)
    Apartado toEntity(CreateApartadoRequest request);

    @Mapping(target = "terrenoNumeroLote", source = "terreno.numeroLote")
    @Mapping(target = "terrenoManzana", source = "terreno.manzana")
    @Mapping(target = "proyectoId", source = "terreno.proyectoId")
    @Mapping(target = "proyectoNombre", source = "terreno.proyecto.nombre")
    ApartadoResponse toResponse(Apartado apartado);

    List<ApartadoResponse> toResponseList(List<Apartado> apartados);
}
