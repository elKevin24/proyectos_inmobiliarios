package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.ProyectoResponse;
import com.inmobiliaria.terrenos.application.dto.proyecto.UpdateProyectoRequest;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Proyecto
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProyectoMapper {

    /**
     * Convierte CreateProyectoRequest a entidad Proyecto
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "terrenos", ignore = true)
    @Mapping(target = "estado", source = "estadoProyecto")
    Proyecto toEntity(CreateProyectoRequest request);

    /**
     * Convierte entidad Proyecto a ProyectoResponse
     */
    @Mapping(target = "estadoProyecto", source = "estado")
    ProyectoResponse toResponse(Proyecto proyecto);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    List<ProyectoResponse> toResponseList(List<Proyecto> proyectos);

    /**
     * Actualiza una entidad Proyecto existente con datos de UpdateProyectoRequest
     * Solo actualiza los campos no nulos del request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "terrenos", ignore = true)
    @Mapping(target = "totalTerrenos", ignore = true)
    @Mapping(target = "terrenosDisponibles", ignore = true)
    @Mapping(target = "terrenosApartados", ignore = true)
    @Mapping(target = "terrenosVendidos", ignore = true)
    @Mapping(target = "estado", source = "estadoProyecto")
    void updateEntityFromRequest(UpdateProyectoRequest request, @MappingTarget Proyecto proyecto);
}
