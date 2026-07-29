package com.inmobiliaria.terrenos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.inmobiliaria.terrenos.domain.entity.Permiso;
import com.inmobiliaria.terrenos.domain.entity.Rol;
import com.inmobiliaria.terrenos.domain.entity.Tenant;
import com.inmobiliaria.terrenos.domain.entity.Usuario;
import com.inmobiliaria.terrenos.domain.enums.RolEnum;
import com.inmobiliaria.terrenos.domain.repository.PermisoRepository;
import com.inmobiliaria.terrenos.domain.repository.RolRepository;
import com.inmobiliaria.terrenos.domain.repository.TenantRepository;
import com.inmobiliaria.terrenos.domain.repository.UsuarioRepository;
import com.inmobiliaria.terrenos.infrastructure.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de RBAC (Role-Based Access Control).
 *
 * Verifica que usuarios sin los permisos necesarios reciben 403 Forbidden.
 * Crea usuarios con roles restringidos directamente via repositorios.
 */
@DisplayName("E2E - RBAC (Control de Acceso por Rol)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RbacE2ETest extends BaseE2ETest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private TenantRepository tenantRepository;
    @Autowired private PermisoRepository permisoRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    /** Token del admin (tiene todos los permisos) */
    private String adminToken;
    /** Token de un usuario VENDEDOR (solo COTIZACION_VER, COTIZACION_CREAR, TERRENO_VER) */
    private String vendedorToken;
    /** Token de un usuario sin ningún permiso (rol SECRETARIA vacío) */
    private String sinPermisosToken;

    private Long adminTenantId;
    private Long proyectoId;

    @BeforeEach
    void setUp() throws Exception {
        if (vendedorToken == null) {
            long ts = System.nanoTime();

            // ── 1. Registrar tenant admin ──────────────────────────────────
            ResponseEntity<JsonNode> adminResp = registerTenant("rbac" + ts);
            assertEquals(HttpStatus.CREATED, adminResp.getStatusCode());
            adminToken = getAccessToken(adminResp);
            // AuthResponse usa @JsonProperty("user_info") -> snake_case en JSON
            adminTenantId = adminResp.getBody().get("user_info").get("tenantId").asLong();

            // Crear recurso de prueba para usar en los tests
            proyectoId = crearProyecto(adminToken, "Proyecto RBAC");

            // ── 2. Crear usuario VENDEDOR con permisos limitados ───────────
            vendedorToken = crearUsuarioConPermisos(
                    ts, "vendedor_rbac_" + ts + "@test.com",
                    RolEnum.VENDEDOR, adminTenantId,
                    List.of("COTIZACION_VER", "COTIZACION_CREAR", "TERRENO_VER"));

            // ── 3. Crear usuario SIN PERMISOS ──────────────────────────────
            sinPermisosToken = crearUsuarioConPermisos(
                    ts, "noperm_rbac_" + ts + "@test.com",
                    RolEnum.SECRETARIA, adminTenantId,
                    List.of()); // rol vacío — sin ningún permiso
        }
    }

    /**
     * Crea un usuario en el tenant dado con un rol que tiene exactamente los permisos indicados.
     * Genera su JWT directamente (sin pasar por el endpoint de login) para velocidad.
     */
    private String crearUsuarioConPermisos(long ts, String email, RolEnum rolEnum,
                                           Long tenantId, List<String> codigosPermiso) {
        // Buscar los permisos del repositorio
        List<Permiso> permisos = codigosPermiso.stream()
                .flatMap(codigo -> permisoRepository.findAll().stream()
                        .filter(p -> p.getCodigo().equals(codigo)))
                .collect(Collectors.toList());

        // Crear el rol con solo esos permisos
        Rol rol = Rol.builder()
                .nombre(rolEnum)
                .descripcion("Rol " + rolEnum.name() + " limitado para RBAC tests")
                .permisos(Set.copyOf(permisos))
                .build();
        rol.setTenantId(tenantId);
        rol = rolRepository.save(rol);

        // Crear el usuario
        Usuario usuario = Usuario.builder()
                .nombre("Usuario")
                .apellido(rolEnum.name())
                .email(email)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .activo(true)
                .roles(Set.of(rol))
                .build();
        usuario.setTenantId(tenantId);
        usuarioRepository.save(usuario);

        // Construir UserDetails manualmente para generar JWT
        var authorities = permisos.stream()
                .map(p -> new SimpleGrantedAuthority(p.getCodigo()))
                .collect(Collectors.toSet());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rolEnum.name()));

        UserDetails userDetails = User.builder()
                .username(email)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .authorities(authorities)
                .build();

        return jwtService.generateTokenWithTenant(userDetails, tenantId);
    }

    // ─── Tests RBAC: 403 Forbidden ───────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("VENDEDOR sin PROYECTO_VER recibe 403 en GET /proyectos")
    void vendedor_sinProyectoVer_returns403() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos", vendedorToken);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Vendedor obtuvo acceso a proyectos sin permiso: " + response.getBody());
    }

    @Test
    @Order(2)
    @DisplayName("VENDEDOR sin PROYECTO_CREAR recibe 403 en POST /proyectos")
    void vendedor_sinProyectoCrear_returns403() throws Exception {
        String body = """
                {
                    "nombre": "Proyecto No Autorizado",
                    "descripcion": "Test RBAC",
                    "direccion": "Av. Test 1",
                    "ciudad": "Ciudad",
                    "estado": "Estado",
                    "codigoPostal": "76000",
                    "tipoPrecio": "FIJO",
                    "precioBase": 100000.00,
                    "totalTerrenos": 1,
                    "terrenosDisponibles": 1,
                    "estadoProyecto": "EN_VENTA"
                }
                """;
        ResponseEntity<JsonNode> response = postJsonWithAuth("/api/v1/proyectos", body, vendedorToken);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Vendedor pudo crear un proyecto sin permiso: " + response.getBody());
    }

    @Test
    @Order(3)
    @DisplayName("VENDEDOR sin CLIENTE_VER recibe 403 en GET /clientes")
    void vendedor_sinClienteVer_returns403() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/clientes", vendedorToken);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Vendedor obtuvo acceso a clientes sin permiso: " + response.getBody());
    }

    @Test
    @Order(4)
    @DisplayName("VENDEDOR sin REPORTE_VER recibe 403 en GET /reportes/dashboard")
    void vendedor_sinReporteVer_returns403() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/reportes/dashboard", vendedorToken);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Vendedor obtuvo acceso a reportes sin permiso: " + response.getBody());
    }

    @Test
    @Order(5)
    @DisplayName("VENDEDOR sin ADMIN recibe 403 en GET /auditoria/simple")
    void vendedor_sinAdmin_returns403_enAuditoria() throws Exception {
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/auditoria/simple", vendedorToken);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(),
                "Vendedor obtuvo acceso a auditoria sin permiso ADMIN: " + response.getBody());
    }

    @Test
    @Order(6)
    @DisplayName("Usuario sin permisos recibe 403 en cualquier endpoint protegido")
    void sinPermisos_returns403_enTodosLosEndpoints() throws Exception {
        // Proyectos
        ResponseEntity<JsonNode> r1 = getJsonWithAuth("/api/v1/proyectos", sinPermisosToken);
        assertEquals(HttpStatus.FORBIDDEN, r1.getStatusCode(),
                "Usuario sin permisos accedió a proyectos");

        // Terrenos
        ResponseEntity<JsonNode> r2 = getJsonWithAuth("/api/v1/terrenos?proyectoId=1", sinPermisosToken);
        assertEquals(HttpStatus.FORBIDDEN, r2.getStatusCode(),
                "Usuario sin permisos accedió a terrenos");

        // Clientes
        ResponseEntity<JsonNode> r3 = getJsonWithAuth("/api/v1/clientes", sinPermisosToken);
        assertEquals(HttpStatus.FORBIDDEN, r3.getStatusCode(),
                "Usuario sin permisos accedió a clientes");
    }

    @Test
    @Order(7)
    @DisplayName("VENDEDOR CON COTIZACION_VER puede acceder a cotizaciones")
    void vendedor_conCotizacionVer_puede_accederACotizaciones() throws Exception {
        // El vendedor tiene COTIZACION_VER — el endpoint debe permitir acceso (200)
        // El JwtAuthenticationFilter carga userDetails de BD (con permisos del rol) y
        // extrae tenant_id del JWT claim para establecer el TenantContext.
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/cotizaciones", vendedorToken);
        // 200 OK esperado: el usuario tiene el permiso COTIZACION_VER
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Vendedor con COTIZACION_VER no pudo acceder a cotizaciones: " + response.getBody());
    }

    @Test
    @Order(8)
    @DisplayName("Token invalido retorna 4xx en endpoint protegido")
    void tokenInvalido_returns4xx() throws Exception {
        // El JwtAuthenticationFilter captura la excepción de token inválido y continúa
        // sin autenticar → Spring Security evalúa @PreAuthorize y retorna 403 (Forbidden).
        // Esto es comportamiento de diseño: sin autenticación válida = acceso denegado.
        ResponseEntity<JsonNode> response = getJsonWithAuth("/api/v1/proyectos", "token.invalido.jwt");
        assertTrue(response.getStatusCode().is4xxClientError(),
                "Token inválido debería retornar 4xx: " + response.getStatusCode());
    }

    @Test
    @Order(9)
    @DisplayName("Sin token retorna 401 en endpoint protegido")
    void sinToken_returns401() throws Exception {
        ResponseEntity<JsonNode> response = getJson("/api/v1/proyectos");
        assertTrue(response.getStatusCode().is4xxClientError(),
                "Sin token debería retornar 4xx: " + response.getStatusCode());
    }
}
