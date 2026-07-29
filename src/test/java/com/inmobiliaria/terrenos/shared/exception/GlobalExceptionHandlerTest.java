package com.inmobiliaria.terrenos.shared.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/test");
    }

    @Test
    @DisplayName("handleResourceNotFoundException - Retorna HTTP 404 Not Found")
    void handleResourceNotFoundException_Retorna404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado con id: 10");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("Recurso no encontrado");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    @DisplayName("handleBusinessException - Retorna estado HTTP configurado en la excepción")
    void handleBusinessException_RetornaStatusConfigurado() {
        BusinessException ex = new BusinessException("Saldo insuficiente para cuota", HttpStatus.CONFLICT);

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).isEqualTo("Saldo insuficiente para cuota");
    }

    @Test
    @DisplayName("handleTenantNotFoundException - Retorna HTTP 403 Forbidden")
    void handleTenantNotFoundException_Retorna403() {
        TenantNotFoundException ex = new TenantNotFoundException("Tenant 99 no existe");

        ResponseEntity<ErrorResponse> response = handler.handleTenantNotFoundException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("handleAuthenticationException - Retorna HTTP 401 Unauthorized")
    void handleAuthenticationException_Retorna401() {
        BadCredentialsException ex = new BadCredentialsException("Contraseña incorrecta");

        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("handleAccessDeniedException - Retorna HTTP 403 Forbidden")
    void handleAccessDeniedException_Retorna403() {
        AccessDeniedException ex = new AccessDeniedException("Sin permisos de admin");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("handleGlobalException - Retorna HTTP 500 Internal Server Error ante fallos no capturados")
    void handleGlobalException_Retorna500() {
        NullPointerException ex = new NullPointerException("NPE en servicio interno");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).contains("error inesperado");
    }
}
