package com.inmobiliaria.terrenos.infrastructure.tenant;

import com.inmobiliaria.terrenos.shared.exception.TenantNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Filtro que extrae el tenant_id del JWT y lo establece en el TenantContext.
 * Se ejecuta en cada petición para garantizar el aislamiento multi-tenant.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    @Value("${app.security.jwt.secret}")
    private String jwtSecret;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Rutas públicas que no requieren tenant_id
            String requestUri = request.getRequestURI();
            if (isPublicEndpoint(requestUri)) {
                log.debug("Public endpoint accessed: {}", requestUri);
                filterChain.doFilter(request, response);
                return;
            }

            // Extraer token JWT del header
            String token = extractTokenFromRequest(request);

            if (token != null) {
                // Extraer tenant_id del JWT
                Long tenantId = extractTenantIdFromToken(token);

                if (tenantId == null) {
                    throw new TenantNotFoundException("Tenant ID no encontrado en el token JWT");
                }

                // Establecer tenant_id en el contexto
                TenantContext.setTenantId(tenantId);
                log.debug("Tenant ID establecido: {} para request: {}", tenantId, requestUri);
            }

            filterChain.doFilter(request, response);

        } finally {
            // CRÍTICO: Limpiar el contexto para evitar memory leaks
            TenantContext.clear();
        }
    }

    /**
     * Extrae el token JWT del header Authorization.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Extrae el tenant_id del token JWT.
     */
    private Long extractTenantIdFromToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object tenantIdClaim = claims.get("tenant_id");

            if (tenantIdClaim instanceof Integer) {
                return ((Integer) tenantIdClaim).longValue();
            } else if (tenantIdClaim instanceof Long) {
                return (Long) tenantIdClaim;
            }

            return null;

        } catch (Exception e) {
            log.error("Error extrayendo tenant_id del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la clave de firma para validar el JWT.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(
                Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8))
        );
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Verifica si el endpoint es público y no requiere tenant_id.
     */
    private boolean isPublicEndpoint(String requestUri) {
        return requestUri.startsWith("/api/v1/auth/") ||
               requestUri.startsWith("/api/v1/public/") ||
               requestUri.startsWith("/actuator/") ||
               requestUri.startsWith("/swagger-ui") ||
               requestUri.startsWith("/api-docs") ||
               requestUri.startsWith("/v3/api-docs") ||
               requestUri.equals("/error");
    }
}
