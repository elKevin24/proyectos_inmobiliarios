package com.inmobiliaria.terrenos.infrastructure.tenant;

import com.inmobiliaria.terrenos.shared.exception.TenantNotFoundException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

    @InjectMocks
    private TenantFilter tenantFilter;

    @Mock
    private FilterChain filterChain;

    private static final String SECRET = "0123456789012345678901234567890123456789012345678901234567890123";
    private SecretKey signingKey;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tenantFilter, "jwtSecret", SECRET);

        byte[] keyBytes = Base64.getDecoder().decode(
                Base64.getEncoder().encodeToString(SECRET.getBytes(StandardCharsets.UTF_8))
        );
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("doFilterInternal - Omite verificación en endpoints públicos como /api/v1/auth/login")
    void doFilterInternal_EndpointPublico_OmiteToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Extrae tenant_id del JWT y lo coloca en TenantContext para peticiones protegidas")
    void doFilterInternal_TokenValido_EstableceTenantContext() throws ServletException, IOException {
        Long expectedTenantId = 42L;
        String token = Jwts.builder()
                .claim("tenant_id", expectedTenantId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(signingKey)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/proyectos");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        tenantFilter.doFilterInternal(request, response, (req, res) -> {
            // Verificar dentro de la cadena que TenantContext contenga el ID correcto
            assertThat(TenantContext.getTenantId()).isEqualTo(expectedTenantId);
        });
    }

    @Test
    @DisplayName("doFilterInternal - Limpia el TenantContext en el bloque finally al terminar la petición")
    void doFilterInternal_LimpiaTenantContextAlFinalizar() throws ServletException, IOException {
        String token = Jwts.builder()
                .claim("tenant_id", 99L)
                .signWith(signingKey)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/terrenos");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        tenantFilter.doFilterInternal(request, response, filterChain);

        // Después del filtro, el thread-local debe ser reseteado
        assertThat(TenantContext.getTenantId()).isNull();
    }
}
