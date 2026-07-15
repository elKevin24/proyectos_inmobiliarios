package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("E2E - Autenticacion")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthE2ETest extends BaseE2ETest {

    private String registeredEmail;

    @Test
    @Order(1)
    @DisplayName("POST /api/v1/auth/register - Registro exitoso")
    void register_createsTenantAndUser_returnsTokens() throws Exception {
        registeredEmail = "admin_auth_" + System.nanoTime() + "@test.com";
        String empresaEmail = "empresa_auth_" + System.nanoTime() + "@test.com";

        String body = """
                {
                    "nombreEmpresa": "Empresa Auth Test",
                    "emailEmpresa": "%s",
                    "nombre": "Admin",
                    "apellido": "Test",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(empresaEmail, registeredEmail, TEST_PASSWORD);

        ResponseEntity<JsonNode> response = postJson("/api/v1/auth/register", body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("access_token").asText());
        assertNotNull(response.getBody().get("refresh_token").asText());
        assertEquals("Bearer", response.getBody().get("token_type").asText());
        assertNotNull(response.getBody().get("expires_in").asLong());

        JsonNode userInfo = response.getBody().get("user_info");
        assertNotNull(userInfo.get("id").asLong());
        assertNotNull(userInfo.get("tenantId").asLong());
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/v1/auth/register - Email duplicado retorna 409")
    void register_duplicateEmail_returnsConflict() throws Exception {
        if (registeredEmail == null) {
            registeredEmail = "admin_dup_" + System.nanoTime() + "@test.com";
            String regBody = """
                    {
                        "nombreEmpresa": "Empresa Primera",
                        "emailEmpresa": "emp_dup1@test.com",
                        "nombre": "Admin",
                        "apellido": "Dup",
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(registeredEmail, TEST_PASSWORD);
            postJson("/api/v1/auth/register", regBody);
        }

        String body = """
                {
                    "nombreEmpresa": "Empresa Duplicada",
                    "emailEmpresa": "dup_empresa@test.com",
                    "nombre": "Admin",
                    "apellido": "Dup",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(registeredEmail, TEST_PASSWORD);

        ResponseEntity<JsonNode> response = postJson("/api/v1/auth/register", body);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/v1/auth/register - Datos invalidos retorna 400")
    void register_invalidData_returnsBadRequest() throws Exception {
        String body = """
                {
                    "nombreEmpresa": "X",
                    "emailEmpresa": "not-an-email",
                    "nombre": "",
                    "apellido": "",
                    "email": "",
                    "password": "short"
                }
                """;

        ResponseEntity<JsonNode> response = postJson("/api/v1/auth/register", body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/v1/auth/login - Login exitoso")
    void login_validCredentials_returnsTokens() throws Exception {
        ensureRegistered();

        ResponseEntity<JsonNode> response = login(registeredEmail, TEST_PASSWORD);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("access_token").asText());
        assertNotNull(response.getBody().get("refresh_token").asText());
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/v1/auth/login - Credenciales invalidas retorna error")
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        ensureRegistered();

        ResponseEntity<JsonNode> response = login(registeredEmail, "WrongPassword123!");
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @Order(6)
    @DisplayName("POST /api/v1/auth/refresh - Refresh token retorna nuevo access token")
    void refreshToken_validRefreshToken_returnsNewAccessToken() throws Exception {
        ensureRegistered();

        ResponseEntity<JsonNode> loginResponse = login(registeredEmail, TEST_PASSWORD);
        String refreshToken = getRefreshToken(loginResponse);

        String body = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        ResponseEntity<JsonNode> response = postJson("/api/v1/auth/refresh", body);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("access_token").asText());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/v1/auth/refresh - Refresh token invalido retorna error")
    void refreshToken_invalidToken_returnsError() throws Exception {
        String body = """
                {
                    "refreshToken": "invalid.token.here"
                }
                """;

        ResponseEntity<JsonNode> response = postJson("/api/v1/auth/refresh", body);
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/v1/proyectos sin token retorna 401/403")
    void accessProtectedEndpoint_withoutToken_returnsUnauthorized() throws Exception {
        ResponseEntity<JsonNode> response = getJson("/api/v1/proyectos");
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @Order(9)
    @DisplayName("GET /api/v1/proyectos con token valido retorna 200")
    void accessProtectedEndpoint_withValidToken_returnsOk() throws Exception {
        ensureRegistered();

        ResponseEntity<JsonNode> loginResponse = login(registeredEmail, TEST_PASSWORD);
        String token = getAccessToken(loginResponse);

        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos", token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private void ensureRegistered() throws Exception {
        if (registeredEmail == null) {
            registeredEmail = "admin_ensure_" + System.nanoTime() + "@test.com";
            String empresaEmail = "emp_ensure_" + System.nanoTime() + "@test.com";
            String regBody = """
                    {
                        "nombreEmpresa": "Empresa Ensure",
                        "emailEmpresa": "%s",
                        "nombre": "Admin",
                        "apellido": "Ensure",
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(empresaEmail, registeredEmail, TEST_PASSWORD);
            postJson("/api/v1/auth/register", regBody);
        }
    }
}
