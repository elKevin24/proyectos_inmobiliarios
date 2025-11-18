package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.archivo.ArchivoResponse;
import com.inmobiliaria.terrenos.domain.entity.Archivo;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Archivo
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ArchivoMapper {

    @Mapping(target = "proyectoNombre", source = "proyecto.nombre")
    @Mapping(target = "terrenoNumeroLote", source = "terreno.numeroLote")
    @Mapping(target = "urlDescarga", expression = "java(\"/api/v1/archivos/\" + archivo.getId() + \"/download\")")
    ArchivoResponse toResponse(Archivo archivo);

    List<ArchivoResponse> toResponseList(List<Archivo> archivos);
}
