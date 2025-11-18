package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.apartado.ApartadoResponse;
import com.inmobiliaria.terrenos.application.dto.apartado.CreateApartadoRequest;
import com.inmobiliaria.terrenos.application.service.ApartadoService;
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
import java.util.Map;

/**
 * Controlador REST para gestión de apartados
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/apartados")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Apartados", description = "Gestión de apartados de terrenos")
@SecurityRequirement(name = "bearerAuth")
public class ApartadoController {

    private final ApartadoService apartadoService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('APARTADO_VER', 'ADMIN')")
    @Operation(summary = "Listar apartados", description = "Obtiene la lista de apartados con filtros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ApartadoResponse.class))))
    })
    public ResponseEntity<List<ApartadoResponse>> listarApartados(
            @Parameter(description = "Solo apartados vigentes")
            @RequestParam(required = false, defaultValue = "false") Boolean vigentes,

            @Parameter(description = "Solo apartados vencidos")
            @RequestParam(required = false, defaultValue = "false") Boolean vencidos
    ) {
        log.info("GET /api/v1/apartados - vigentes: {}, vencidos: {}", vigentes, vencidos);

        List<ApartadoResponse> apartados;
        if (vigentes) {
            apartados = apartadoService.listarApartadosVigentes();
        } else if (vencidos) {
            apartados = apartadoService.listarApartadosVencidos();
        } else {
            apartados = apartadoService.listarApartados();
        }

        return ResponseEntity.ok(apartados);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('APARTADO_VER', 'ADMIN')")
    @Operation(summary = "Obtener apartado por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Apartado encontrado",
                    content = @Content(schema = @Schema(implementation = ApartadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<ApartadoResponse> obtenerApartado(@PathVariable Long id) {
        log.info("GET /api/v1/apartados/{}", id);
        return ResponseEntity.ok(apartadoService.obtenerApartado(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('APARTADO_CREAR', 'ADMIN')")
    @Operation(summary = "Crear apartado", description = "Crea un nuevo apartado y cambia el estado del terreno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Apartado creado",
                    content = @Content(schema = @Schema(implementation = ApartadoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Terreno no encontrado"),
            @ApiResponse(responseCode = "409", description = "Terreno no disponible")
    })
    public ResponseEntity<ApartadoResponse> crearApartado(@Valid @RequestBody CreateApartadoRequest request) {
        log.info("POST /api/v1/apartados - Terreno: {}, Cliente: {}", request.getTerrenoId(), request.getClienteNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(apartadoService.crearApartado(request));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('APARTADO_EDITAR', 'ADMIN')")
    @Operation(summary = "Cancelar apartado", description = "Cancela un apartado y libera el terreno")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Apartado cancelado",
                    content = @Content(schema = @Schema(implementation = ApartadoResponse.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede cancelar")
    })
    public ResponseEntity<ApartadoResponse> cancelarApartado(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        log.info("PUT /api/v1/apartados/{}/cancelar", id);
        String motivo = body != null ? body.get("motivo") : null;
        return ResponseEntity.ok(apartadoService.cancelarApartado(id, motivo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('APARTADO_ELIMINAR', 'ADMIN')")
    @Operation(summary = "Eliminar apartado", description = "Elimina un apartado (solo cancelados o vencidos)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Apartado eliminado"),
            @ApiResponse(responseCode = "404", description = "No encontrado"),
            @ApiResponse(responseCode = "409", description = "Apartado vigente no se puede eliminar")
    })
    public ResponseEntity<Void> eliminarApartado(@PathVariable Long id) {
        log.info("DELETE /api/v1/apartados/{}", id);
        apartadoService.eliminarApartado(id);
        return ResponseEntity.noContent().build();
    }
}
