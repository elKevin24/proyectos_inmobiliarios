package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E2E - Plano Ingesta (CV + SSE + Confirmación Multi-tenant)")
class PlanoIngestaE2ETest extends BaseE2ETest {

    @Autowired
    private PlanoStatusStore statusStore;

    private String tokenTenantA;
    private String tokenTenantB;
    private Long proyectoIdA;
    private Long proyectoIdB;

    @BeforeEach
    void init() throws Exception {
        setupPermisos();
        if (tokenTenantA == null) {
            String suffixA = "planoA_" + UUID.randomUUID().toString().substring(0, 6);
            String suffixB = "planoB_" + UUID.randomUUID().toString().substring(0, 6);

            tokenTenantA = getAccessToken(registerTenant(suffixA));
            tokenTenantB = getAccessToken(registerTenant(suffixB));

            proyectoIdA = crearProyecto(tokenTenantA, "Residencial Plano A");
            proyectoIdB = crearProyecto(tokenTenantB, "Residencial Plano B");
        }
    }

    @Test
    @DisplayName("POST /analizar retorna 400 cuando falta nombreAlmacenado")
    void analizarPlanoSinNombreRetorna400() throws Exception {
        ResponseEntity<JsonNode> res = postJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA + "/planos/analizar",
                "{}",
                tokenTenantA
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().get("error").asText()).contains("nombreAlmacenado");
    }

    @Test
    @DisplayName("POST /analizar bloquea acceso cruzado entre tenants con 404")
    void analizarPlanoDeOtroTenantRetorna404() throws Exception {
        String body = """
                {
                    "nombreAlmacenado": "plano_test.png"
                }
                """;
        ResponseEntity<JsonNode> res = postJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA + "/planos/analizar",
                body,
                tokenTenantB
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /analizar retorna 202 Accepted con tareaId y sseUrl")
    void analizarPlanoValidoRetorna202() throws Exception {
        String body = """
                {
                    "nombreAlmacenado": "plano_maestro.png"
                }
                """;
        ResponseEntity<JsonNode> res = postJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA + "/planos/analizar",
                body,
                tokenTenantA
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(res.getBody().get("tareaId").asText()).isNotBlank();
        assertThat(res.getBody().get("sseUrl").asText())
                .startsWith("/api/v1/proyectos/" + proyectoIdA + "/planos/estado/");
    }

    @Test
    @DisplayName("GET /estado/{tareaId} sin token es rechazado con 403")
    void estadoSseSinTokenRetorna403() {
        String tareaId = UUID.randomUUID().toString();
        HttpEntity<Void> req = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> res = getClient().exchange(
                baseUrl() + "/api/v1/proyectos/" + proyectoIdA + "/planos/estado/" + tareaId,
                HttpMethod.GET,
                req,
                String.class
        );

        assertThat(res.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("GET /estado/{tareaId}?token=... autentica por query param y entrega evento SSE cacheado")
    void estadoSseConTokenQueryParamEntregaEvento() {
        String tareaId = UUID.randomUUID().toString();
        statusStore.notificar(tareaId, "completado", "{\"total_lotes_detectados\":2,\"lotes\":[]}");

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.TEXT_EVENT_STREAM));
        HttpEntity<Void> req = new HttpEntity<>(headers);

        ResponseEntity<String> res = getClient().exchange(
                baseUrl() + "/api/v1/proyectos/" + proyectoIdA + "/planos/estado/" + tareaId + "?token=" + tokenTenantA,
                HttpMethod.GET,
                req,
                String.class
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("event:completado");
        assertThat(res.getBody()).contains("total_lotes_detectados");
    }

    @Test
    @DisplayName("POST /confirmar bloquea ingesta cruzada en proyecto de otro tenant con 404")
    void confirmarIngestaDeOtroTenantRetorna404() throws Exception {
        String body = """
                {
                    "lotes": [
                        {
                            "numeroLote": "L-101",
                            "area": 180.5,
                            "coordenadasPlanoJson": "[{\\"x\\":10,\\"y\\":10},{\\"x\\":50,\\"y\\":50}]"
                        }
                    ]
                }
                """;
        ResponseEntity<JsonNode> res = postJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA + "/planos/confirmar",
                body,
                tokenTenantB
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("POST /confirmar con lista vacia retorna 400")
    void confirmarIngestaListaVaciaRetorna400() throws Exception {
        String body = """
                {
                    "lotes": []
                }
                """;
        ResponseEntity<JsonNode> res = postJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA + "/planos/confirmar",
                body,
                tokenTenantA
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /confirmar persiste lotes DISPONIBLES y actualiza contadores del proyecto")
    void confirmarIngestaExitosaPersisteYActualizaProyecto() throws Exception {
        String body = """
                {
                    "lotes": [
                        {
                            "numeroLote": "CV-01",
                            "area": 210.0,
                            "coordenadasPlanoJson": "{\\"tipo\\":\\"poligono\\",\\"puntos\\":[{\\"x\\":0,\\"y\\":0},{\\"x\\":100,\\"y\\":0},{\\"x\\":100,\\"y\\":100}]}"
                        },
                        {
                            "numeroLote": "CV-02",
                            "area": 225.5,
                            "coordenadasPlanoJson": "{\\"tipo\\":\\"poligono\\",\\"puntos\\":[{\\"x\\":100,\\"y\\":0},{\\"x\\":200,\\"y\\":0},{\\"x\\":200,\\"y\\":100}]}"
                        }
                    ]
                }
                """;

        ResponseEntity<JsonNode> res = postJsonWithAuth(
                "/api/v1/proyectos/" + proyectoIdA + "/planos/confirmar",
                body,
                tokenTenantA
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("totalLotes").asInt()).isEqualTo(2);

        ResponseEntity<JsonNode> proyectoRes = getJsonWithAuth("/api/v1/proyectos/" + proyectoIdA, tokenTenantA);
        assertThat(proyectoRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(proyectoRes.getBody().get("totalTerrenos").asInt()).isEqualTo(2);
        assertThat(proyectoRes.getBody().get("terrenosDisponibles").asInt()).isEqualTo(2);
    }
}
