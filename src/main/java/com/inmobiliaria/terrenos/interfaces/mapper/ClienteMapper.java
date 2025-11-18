package com.inmobiliaria.terrenos.interfaces.mapper;

import com.inmobiliaria.terrenos.application.dto.cliente.ClienteResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.CreateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.UpdateClienteRequest;
import com.inmobiliaria.terrenos.domain.entity.Cliente;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper de MapStruct para la entidad Cliente
 *
 * @author Kevin
 * @version 1.0.0
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ClienteMapper {

    /**
     * Convierte CreateClienteRequest a entidad Cliente
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Cliente toEntity(CreateClienteRequest request);

    /**
     * Convierte entidad Cliente a ClienteResponse
     */
    @Mapping(target = "nombreCompleto", expression = "java(cliente.getNombreCompleto())")
    @Mapping(target = "origenDescripcion", expression = "java(cliente.getOrigen() != null ? cliente.getOrigen().getDescripcion() : null)")
    @Mapping(target = "estadoClienteDescripcion", expression = "java(cliente.getEstadoCliente() != null ? cliente.getEstadoCliente().getDescripcion() : null)")
    @Mapping(target = "totalCotizaciones", ignore = true)
    @Mapping(target = "totalApartados", ignore = true)
    @Mapping(target = "totalVentas", ignore = true)
    ClienteResponse toResponse(Cliente cliente);

    /**
     * Convierte lista de entidades a lista de respuestas
     */
    List<ClienteResponse> toResponseList(List<Cliente> clientes);

    /**
     * Actualiza una entidad Cliente existente con datos de UpdateClienteRequest
     * Solo actualiza los campos no nulos del request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(UpdateClienteRequest request, @MappingTarget Cliente cliente);
}
