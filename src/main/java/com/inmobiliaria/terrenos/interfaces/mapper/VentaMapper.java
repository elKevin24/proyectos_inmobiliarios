package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.venta.CreateVentaRequest;
import com.inmobiliaria.terrenos.application.dto.venta.VentaResponse;
import com.inmobiliaria.terrenos.domain.entity.Venta;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Venta
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface VentaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    @Mapping(target = "fechaVenta", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "terreno", ignore = true)
    @Mapping(target = "apartado", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Venta toEntity(CreateVentaRequest request);

    @Mapping(target = "terrenoNumeroLote", source = "terreno.numeroLote")
    @Mapping(target = "terrenoManzana", source = "terreno.manzana")
    @Mapping(target = "proyectoId", source = "terreno.proyectoId")
    @Mapping(target = "proyectoNombre", source = "terreno.proyecto.nombre")
    @Mapping(target = "usuarioNombre", expression = "java(venta.getUsuario() != null ? venta.getUsuario().getNombre() + \" \" + venta.getUsuario().getApellido() : null)")
    VentaResponse toResponse(Venta venta);

    List<VentaResponse> toResponseList(List<Venta> ventas);
}
