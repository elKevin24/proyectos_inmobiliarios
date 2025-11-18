package com.inmobiliaria.terrenos.interfaces.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.plano.CoordenadasPlano;
import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.TerrenoResponse;
import com.inmobiliaria.terrenos.application.dto.terreno.UpdateTerrenoRequest;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

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
public abstract class TerrenoMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Convierte CreateTerrenoRequest a entidad Terreno
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "poligono", ignore = true)
    @Mapping(target = "coordenadasPlano", expression = "java(coordenadasToJson(request.getCoordenadasPlano()))")
    public abstract Terreno toEntity(CreateTerrenoRequest request);

    /**
     * Convierte entidad Terreno a TerrenoResponse
     */
    public abstract TerrenoResponse toResponse(Terreno terreno);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    public abstract List<TerrenoResponse> toResponseList(List<Terreno> terrenos);

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
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "poligono", ignore = true)
    public abstract void updateEntityFromRequest(UpdateTerrenoRequest request, @MappingTarget Terreno terreno);

    /**
     * Actualiza coordenadas de un terreno
     */
    @AfterMapping
    protected void afterUpdate(UpdateTerrenoRequest request, @MappingTarget Terreno terreno) {
        if (request.getCoordenadasPlano() != null) {
            terreno.setCoordenadasPlano(coordenadasToJson(request.getCoordenadasPlano()));
        }
    }

    /**
     * Convierte CoordenadasPlano a JSON string
     */
    protected String coordenadasToJson(CoordenadasPlano coordenadas) {
        if (coordenadas == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(coordenadas);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir coordenadas a JSON", e);
        }
    }

    /**
     * Convierte JSON string a CoordenadasPlano
     */
    protected CoordenadasPlano jsonToCoordenadas(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CoordenadasPlano.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al parsear coordenadas JSON", e);
        }
    }
}
