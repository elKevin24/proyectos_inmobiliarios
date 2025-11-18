package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.cliente.ClienteHistorialResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.ClienteResponse;
import com.inmobiliaria.terrenos.application.dto.cliente.CreateClienteRequest;
import com.inmobiliaria.terrenos.application.dto.cliente.UpdateClienteRequest;
import com.inmobiliaria.terrenos.application.service.ClienteService;
import com.inmobiliaria.terrenos.domain.enums.EstadoCliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de clientes
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clientes", description = "Gestión de clientes y compradores")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Lista clientes con filtros opcionales
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('CLIENTE_VER', 'ADMIN')")
    @Operation(
            summary = "Listar clientes",
            description = "Obtiene la lista de clientes con filtros opcionales (estado, nombre, activos)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de clientes obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ClienteResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<ClienteResponse>> listarClientes(
            @Parameter(description = "Filtrar por estado del cliente")
            @RequestParam(required = false) EstadoCliente estado,

            @Parameter(description = "Buscar por nombre (búsqueda parcial)")
            @RequestParam(required = false) String nombre,

            @Parameter(description = "Solo clientes activos (no inactivos)")
            @RequestParam(required = false, defaultValue = "false") Boolean activos
    ) {
        log.info("GET /api/v1/clientes - estado: {}, nombre: {}, activos: {}", estado, nombre, activos);

        List<ClienteResponse> clientes;

        if (nombre != null && !nombre.isBlank()) {
            clientes = clienteService.buscarClientesPorNombre(nombre);
        } else if (estado != null) {
            clientes = clienteService.listarClientesPorEstado(estado);
        } else if (activos) {
            clientes = clienteService.listarClientesActivos();
        } else {
            clientes = clienteService.listarClientes();
        }

        return ResponseEntity.ok(clientes);
    }

    /**
     * Obtiene un cliente por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE_VER', 'ADMIN')")
    @Operation(
            summary = "Obtener cliente por ID",
            description = "Obtiene los datos completos de un cliente incluyendo estadísticas de transacciones"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ClienteResponse> obtenerCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/clientes/{}", id);
        ClienteResponse cliente = clienteService.obtenerCliente(id);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Obtiene el historial completo de transacciones de un cliente
     */
    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyAuthority('CLIENTE_VER', 'ADMIN')")
    @Operation(
            summary = "Obtener historial del cliente",
            description = "Obtiene el historial completo de transacciones del cliente (cotizaciones, apartados, ventas)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Historial obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteHistorialResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ClienteHistorialResponse> obtenerHistorialCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/v1/clientes/{}/historial", id);
        ClienteHistorialResponse historial = clienteService.obtenerHistorialCliente(id);
        return ResponseEntity.ok(historial);
    }

    /**
     * Crea un nuevo cliente
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('CLIENTE_CREAR', 'ADMIN')")
    @Operation(
            summary = "Crear cliente",
            description = "Crea un nuevo cliente en el sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un cliente con ese email/RFC"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ClienteResponse> crearCliente(
            @Valid @RequestBody CreateClienteRequest request
    ) {
        log.info("POST /api/v1/clientes - Nombre: {} {}", request.getNombre(), request.getApellido());
        ClienteResponse cliente = clienteService.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    /**
     * Actualiza un cliente existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE_EDITAR', 'ADMIN')")
    @Operation(
            summary = "Actualizar cliente",
            description = "Actualiza los datos de un cliente existente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "409", description = "Ya existe otro cliente con ese email"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateClienteRequest request
    ) {
        log.info("PUT /api/v1/clientes/{}", id);
        ClienteResponse cliente = clienteService.actualizarCliente(id, request);
        return ResponseEntity.ok(cliente);
    }

    /**
     * Elimina un cliente (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('CLIENTE_ELIMINAR', 'ADMIN')")
    @Operation(
            summary = "Eliminar cliente",
            description = "Elimina un cliente (soft delete). No se puede eliminar si tiene transacciones activas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar porque tiene transacciones activas"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Void> eliminarCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/clientes/{}", id);
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
