package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.fase.CreateFaseRequest;
import com.inmobiliaria.terrenos.application.dto.fase.FaseResponse;
import com.inmobiliaria.terrenos.application.dto.fase.UpdateFaseRequest;
import com.inmobiliaria.terrenos.domain.entity.Fase;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Fase
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FaseMapper {

    /**
     * Convierte CreateFaseRequest a entidad Fase
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "proyecto", ignore = true)
    @Mapping(target = "terrenos", ignore = true)
    Fase toEntity(CreateFaseRequest request);

    /**
     * Convierte entidad Fase a FaseResponse
     */
    @Mapping(target = "proyectoNombre", source = "proyecto.nombre")
    FaseResponse toResponse(Fase fase);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    List<FaseResponse> toResponseList(List<Fase> fases);

    /**
     * Actualiza una entidad Fase existente con datos de UpdateFaseRequest
     * Solo actualiza los campos no nulos del request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "proyectoId", ignore = true)
    @Mapping(target = "totalTerrenos", ignore = true)
    @Mapping(target = "terrenosDisponibles", ignore = true)
    @Mapping(target = "terrenosApartados", ignore = true)
    @Mapping(target = "terrenosVendidos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "proyecto", ignore = true)
    @Mapping(target = "terrenos", ignore = true)
    void updateEntityFromRequest(UpdateFaseRequest request, @MappingTarget Fase fase);
}
