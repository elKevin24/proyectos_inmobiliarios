package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = com.inmobiliaria.terrenos.TerrenosSaasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected ObjectMapper objectMapper;

    private RestTemplate client;

    @Autowired
    private com.inmobiliaria.terrenos.domain.repository.PermisoRepository permisoRepository;

    @org.junit.jupiter.api.BeforeEach
    protected void setupPermisos() {
        // All permissions are seeded via data-test.sql (single source of truth)
        // This method is kept as a safety net fallback
        if (permisoRepository.count() == 0) {
            String[][] permisos = {
                {"PROYECTO_CREAR", "Crear Proyecto", "PROYECTO"},
                {"PROYECTO_VER", "Ver Proyecto", "PROYECTO"},
                {"PROYECTO_EDITAR", "Editar Proyecto", "PROYECTO"},
                {"PROYECTO_ELIMINAR", "Eliminar Proyecto", "PROYECTO"},
                {"TERRENO_VER", "Ver Terreno", "TERRENO"},
                {"TERRENO_CREAR", "Crear Terreno", "TERRENO"},
                {"TERRENO_EDITAR", "Editar Terreno", "TERRENO"},
                {"TERRENO_ELIMINAR", "Eliminar Terreno", "TERRENO"},
                {"COTIZACION_VER", "Ver Cotizacion", "COTIZACION"},
                {"COTIZACION_CREAR", "Crear Cotizacion", "COTIZACION"},
                {"COTIZACION_ELIMINAR", "Eliminar Cotizacion", "COTIZACION"},
                {"APARTADO_VER", "Ver Apartado", "APARTADO"},
                {"APARTADO_CREAR", "Crear Apartado", "APARTADO"},
                {"APARTADO_EDITAR", "Editar Apartado", "APARTADO"},
                {"APARTADO_ELIMINAR", "Eliminar Apartado", "APARTADO"},
                {"VENTA_VER", "Ver Venta", "VENTA"},
                {"VENTA_CREAR", "Crear Venta", "VENTA"},
                {"VENTA_EDITAR", "Editar Venta", "VENTA"},
                {"VENTA_ELIMINAR", "Eliminar Venta", "VENTA"},
                {"REPORTE_VER", "Ver Reportes", "REPORTE"},
                {"ARCHIVO_VER", "Ver Archivos", "ARCHIVO"},
                {"ARCHIVO_CREAR", "Crear Archivos", "ARCHIVO"},
                {"ARCHIVO_ELIMINAR", "Eliminar Archivos", "ARCHIVO"},
                {"CLIENTE_VER", "Ver Clientes", "CLIENTE"},
                {"CLIENTE_CREAR", "Crear Clientes", "CLIENTE"},
                {"CLIENTE_EDITAR", "Editar Clientes", "CLIENTE"},
                {"CLIENTE_ELIMINAR", "Eliminar Clientes", "CLIENTE"},
                {"PLAN_PAGO_VER", "Ver Planes de Pago", "PLAN_PAGO"},
                {"PLAN_PAGO_CREAR", "Crear Planes de Pago", "PLAN_PAGO"},
                {"PLAN_PAGO_EDITAR", "Editar Planes de Pago", "PLAN_PAGO"},
                {"PAGO_REGISTRAR", "Registrar Pagos", "PAGO"},
                {"ADMIN", "Administrador Total", "ADMIN"}
            };
            for (String[] p : permisos) {
                permisoRepository.save(com.inmobiliaria.terrenos.domain.entity.Permiso.builder()
                        .codigo(p[0]).nombre(p[1]).modulo(p[2]).build());
            }
        }
    }

    protected RestTemplate getClient() {
        if (client == null) {
            client = new RestTemplate(new JdkClientHttpRequestFactory());
            client.setErrorHandler(new org.springframework.web.client.NoOpResponseErrorHandler());
        }
        return client;
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected static final String TEST_PASSWORD = "Admin123!";

    protected JsonNode parseJson(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody);
    }

    protected ResponseEntity<JsonNode> postJson(String path, String body) throws Exception {
        HttpEntity<String> request = new HttpEntity<>(body, jsonHeaders());
        ResponseEntity<String> response = getClient().postForEntity(
                baseUrl() + path, request, String.class);
        JsonNode jsonBody = response.hasBody() ? parseJson(response.getBody()) : null;
        return new ResponseEntity<>(jsonBody, response.getHeaders(), response.getStatusCode());
    }

    protected ResponseEntity<JsonNode> postJsonWithAuth(String path, String body, String token) throws Exception {
        HttpEntity<String> request = new HttpEntity<>(body, authHeaders(token));
        ResponseEntity<String> response = getClient().postForEntity(
                baseUrl() + path, request, String.class);
        JsonNode jsonBody = response.hasBody() ? parseJson(response.getBody()) : null;
        return new ResponseEntity<>(jsonBody, response.getHeaders(), response.getStatusCode());
    }

    protected ResponseEntity<JsonNode> getJson(String path) throws Exception {
        HttpEntity<Void> request = new HttpEntity<>(jsonHeaders());
        ResponseEntity<String> response = getClient().exchange(
                baseUrl() + path, HttpMethod.GET, request, String.class);
        JsonNode jsonBody = response.hasBody() ? parseJson(response.getBody()) : null;
        return new ResponseEntity<>(jsonBody, response.getHeaders(), response.getStatusCode());
    }

    protected ResponseEntity<JsonNode> getJsonWithAuth(String path, String token) throws Exception {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(token));
        ResponseEntity<String> response = getClient().exchange(
                baseUrl() + path, HttpMethod.GET, request, String.class);
        JsonNode jsonBody = response.hasBody() ? parseJson(response.getBody()) : null;
        return new ResponseEntity<>(jsonBody, response.getHeaders(), response.getStatusCode());
    }

    protected ResponseEntity<JsonNode> putJson(String path, String body, String token) throws Exception {
        HttpEntity<String> request = new HttpEntity<>(body, authHeaders(token));
        ResponseEntity<String> response = getClient().exchange(
                baseUrl() + path, HttpMethod.PUT, request, String.class);
        JsonNode jsonBody = response.hasBody() ? parseJson(response.getBody()) : null;
        return new ResponseEntity<>(jsonBody, response.getHeaders(), response.getStatusCode());
    }

    protected ResponseEntity<JsonNode> patchJson(String path, String token) throws Exception {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(token));
        ResponseEntity<String> response = getClient().exchange(
                baseUrl() + path, HttpMethod.PATCH, request, String.class);
        JsonNode jsonBody = response.hasBody() ? parseJson(response.getBody()) : null;
        return new ResponseEntity<>(jsonBody, response.getHeaders(), response.getStatusCode());
    }

    protected ResponseEntity<Void> deleteJson(String path, String token) throws Exception {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders(token));
        return getClient().exchange(
                baseUrl() + path, HttpMethod.DELETE, request, Void.class);
    }

    protected ResponseEntity<JsonNode> postMultipartWithAuth(String path, String paramName,
            org.springframework.core.io.Resource fileResource, String token,
            java.util.Map<String, String> extraParams) throws Exception {
        org.springframework.util.LinkedMultiValueMap<String, Object> parts = new org.springframework.util.LinkedMultiValueMap<>();
        parts.add(paramName, fileResource);
        if (extraParams != null) {
            extraParams.forEach(parts::add);
        }
        HttpHeaders headers = authHeaders(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> request =
                new HttpEntity<>(parts, headers);
        ResponseEntity<String> response = getClient().postForEntity(
                baseUrl() + path, request, String.class);
        JsonNode jsonBody = response.hasBody() ? parseJson(response.getBody()) : null;
        return new ResponseEntity<>(jsonBody, response.getHeaders(), response.getStatusCode());
    }

    protected ResponseEntity<JsonNode> registerTenant(String uniqueSuffix) throws Exception {
        String email = "admin_" + uniqueSuffix + "@test.com";
        String empresaEmail = "empresa_" + uniqueSuffix + "@test.com";

        String body = """
                {
                    "nombreEmpresa": "Empresa Test %s",
                    "emailEmpresa": "%s",
                    "nombre": "Admin",
                    "apellido": "Test",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(uniqueSuffix, empresaEmail, email, TEST_PASSWORD);

        return postJson("/api/v1/auth/register", body);
    }

    protected ResponseEntity<JsonNode> login(String email, String password) throws Exception {
        String body = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        return postJson("/api/v1/auth/login", body);
    }

    protected String getAccessToken(ResponseEntity<JsonNode> authResponse) {
        return authResponse.getBody().get("access_token").asText();
    }

    protected String getRefreshToken(ResponseEntity<JsonNode> authResponse) {
        return authResponse.getBody().get("refresh_token").asText();
    }

    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = jsonHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    protected HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected Long crearProyecto(String token, String nombre) throws Exception {
        String body = """
                {
                    "nombre": "%s",
                    "descripcion": "Proyecto de prueba E2E",
                    "direccion": "Av. Test 100",
                    "ciudad": "Queretaro",
                    "estado": "Queretaro",
                    "codigoPostal": "76000",
                    "tipoPrecio": "FIJO",
                    "precioBase": 400000.00,
                    "totalTerrenos": 10,
                    "terrenosDisponibles": 10,
                    "estadoProyecto": "EN_VENTA"
                }
                """.formatted(nombre);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/proyectos", body, token);
        return response.getBody().get("id").asLong();
    }

    protected Long crearTerreno(String token, Long proyectoId, String numeroLote) throws Exception {
        String body = """
                {
                    "proyectoId": %d,
                    "numeroLote": "%s",
                    "manzana": "A",
                    "area": 200.00,
                    "precioBase": 400000.00,
                    "precioFinal": 400000.00
                }
                """.formatted(proyectoId, numeroLote);

        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/terrenos", body, token);
        return response.getBody().get("id").asLong();
    }
}
