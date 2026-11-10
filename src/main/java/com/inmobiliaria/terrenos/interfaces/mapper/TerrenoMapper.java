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
    @Mapping(target = "caracteristicas", source = "caracteristicas", qualifiedByName = "caracteristicasToJson")
    public abstract Terreno toEntity(CreateTerrenoRequest request);

    /**
     * Convierte entidad Terreno a TerrenoResponse
     */
    @Mapping(target = "caracteristicas", source = "caracteristicas", qualifiedByName = "jsonToCaracteristicas")
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
    @Mapping(target = "coordenadasPlano", ignore = true)
    @Mapping(target = "caracteristicas", ignore = true)
    public abstract void updateEntityFromRequest(UpdateTerrenoRequest request, @MappingTarget Terreno terreno);

    /**
     * Actualiza coordenadas de un terreno
     */
    @AfterMapping
    protected void afterUpdate(UpdateTerrenoRequest request, @MappingTarget Terreno terreno) {
        if (request.getCoordenadasPlano() != null) {
            terreno.setCoordenadasPlano(coordenadasToJson(request.getCoordenadasPlano()));
        }
        if (request.getCaracteristicas() != null) {
            terreno.setCaracteristicas(caracteristicasToJson(request.getCaracteristicas()));
        }
    }

    /**
     * Convierte texto plano o JSON de caracteristicas a JSON válido para la columna jsonb.
     */
    @Named("caracteristicasToJson")
    protected String caracteristicasToJson(String caracteristicas) {
        return Terreno.normalizeJson(caracteristicas);
    }

    /**
     * Deserializa o desenvuelve el valor JSON de caracteristicas para la respuesta DTO.
     */
    @Named("jsonToCaracteristicas")
    protected String jsonToCaracteristicas(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        String trimmed = json.trim();
        // Si es una cadena JSON entrecomillada ("..."), desenvolver al texto original
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            try {
                return objectMapper.readValue(trimmed, String.class);
            } catch (Exception e) {
                return trimmed.substring(1, trimmed.length() - 1);
            }
        }
        // Si es un objeto JSON simple que envuelve descripcion o nombre, extraer el texto
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(trimmed);
                if (node.hasNonNull("nombre") && node.size() == 1) {
                    return node.get("nombre").asText();
                }
                if (node.hasNonNull("descripcion") && node.size() == 1) {
                    return node.get("descripcion").asText();
                }
            } catch (Exception ignored) {
            }
        }
        return trimmed;
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
