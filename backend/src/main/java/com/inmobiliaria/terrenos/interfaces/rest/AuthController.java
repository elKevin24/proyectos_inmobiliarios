package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.auth.AuthResponse;
import com.inmobiliaria.terrenos.application.dto.auth.LoginRequest;
import com.inmobiliaria.terrenos.application.dto.auth.RefreshTokenRequest;
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
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y registro de usuarios
 *
 * @author Kevin
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para iniciar sesión
     *
     * @param request Credenciales de login (email y password)
     * @return Token JWT y información del usuario
     */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario y devuelve tokens JWT (access y refresh)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario o empresa desactivados"
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        log.info("Login exitoso para: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar una nueva empresa con su usuario administrador
     *
     * @param request Datos de la empresa y usuario administrador
     * @return Token JWT y información del usuario creado
     */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar nueva empresa",
            description = "Crea una nueva empresa (tenant) con su usuario administrador y rol ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Empresa registrada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos en la solicitud"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El email de la empresa o usuario ya existe"
            )
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - Empresa: {}, Email usuario: {}",
                request.getNombreEmpresa(), request.getEmail());
        AuthResponse response = authService.register(request);
        log.info("Registro exitoso para empresa: {}", request.getNombreEmpresa());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para refrescar el access token
     *
     * @param request Refresh token válido
     * @return Nuevo access token
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar access token",
            description = "Genera un nuevo access token usando un refresh token válido"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refrescado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token inválido o expirado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuario desactivado"
            )
    })
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/v1/auth/refresh");
        AuthResponse response = authService.refreshToken(request);
        log.info("Token refrescado exitosamente");
        return ResponseEntity.ok(response);
    }
}
