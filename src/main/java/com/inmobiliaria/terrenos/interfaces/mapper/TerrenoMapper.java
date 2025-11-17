package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.TerrenoResponse;
import com.inmobiliaria.terrenos.application.dto.terreno.UpdateTerrenoRequest;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Terreno
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TerrenoMapper {

    /**
     * Convierte CreateTerrenoRequest a entidad Terreno
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "proyecto", ignore = true)
    @Mapping(target = "fase", ignore = true)
    Terreno toEntity(CreateTerrenoRequest request);

    /**
     * Convierte entidad Terreno a TerrenoResponse
     */
    @Mapping(target = "proyectoNombre", source = "proyecto.nombre")
    @Mapping(target = "faseNombre", source = "fase.nombre")
    TerrenoResponse toResponse(Terreno terreno);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    List<TerrenoResponse> toResponseList(List<Terreno> terrenos);

    /**
     * Actualiza una entidad Terreno existente con datos de UpdateTerrenoRequest
     * Solo actualiza los campos no nulos del request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "proyectoId", ignore = true)
    @Mapping(target = "faseId", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "proyecto", ignore = true)
    @Mapping(target = "fase", ignore = true)
    void updateEntityFromRequest(UpdateTerrenoRequest request, @MappingTarget Terreno terreno);
}
