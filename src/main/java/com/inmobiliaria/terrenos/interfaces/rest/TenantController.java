package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.RegisterRequest;
import com.inmobiliaria.terrenos.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para registro público de tenants.
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenants", description = "Registro público de empresas/tenants")
public class TenantController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar tenant", description = "Crea una nueva empresa con su usuario administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tenant registrado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El email ya existe")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/tenants/register - Empresa: {}, Email: {}", request.getNombreEmpresa(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}
