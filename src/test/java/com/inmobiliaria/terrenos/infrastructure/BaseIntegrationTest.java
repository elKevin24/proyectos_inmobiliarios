package com.inmobiliaria.terrenos.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Clase base para pruebas de integración de Spring Boot.
 * Utiliza H2 en memoria con dialecto PostgreSQL compatible
 * y autogeneración de esquema Hibernate (evita el requerimiento de Docker/Testcontainers).
 *
 * @author Kevin
 * @version 1.0.0
 */
@SpringBootTest(classes = com.inmobiliaria.terrenos.TerrenosSaasApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        // Usar base de datos H2 en memoria con compatibilidad PostgreSQL
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:terrenos_test_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        
        // Configurar JPA para crear y eliminar el esquema dinámicamente basado en las entidades Java
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        
        // Desactivar Flyway en los tests de integración para evitar fallos de sintaxis DDL de scripts PostgreSQL reales
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.inmobiliaria.terrenos.domain.repository.PermisoRepository permisoRepository;

    @org.junit.jupiter.api.BeforeEach
    protected void setupPermisos() {
        if (permisoRepository.count() == 0) {
            java.util.List.of(
                    // Proyectos
                    "PROYECTO_CREAR", "PROYECTO_VER", "PROYECTO_EDITAR", "PROYECTO_ELIMINAR",
                    // Terrenos
                    "TERRENO_CREAR", "TERRENO_VER", "TERRENO_EDITAR", "TERRENO_ELIMINAR",
                    // Cotizaciones
                    "COTIZACION_CREAR", "COTIZACION_VER", "COTIZACION_ELIMINAR",
                    // Apartados
                    "APARTADO_CREAR", "APARTADO_EDITAR", "APARTADO_CANCELAR", "APARTADO_VER",
                    // Ventas
                    "VENTA_CREAR", "VENTA_EDITAR", "VENTA_CANCELAR", "VENTA_VER",
                    // Usuarios
                    "USUARIO_CREAR", "USUARIO_EDITAR", "USUARIO_ELIMINAR", "USUARIO_VER", "USUARIO_ASIGNAR_ROLES",
                    // Reportes
                    "REPORTE_VER", "REPORTE_VENTAS", "REPORTE_DISPONIBILIDAD", "REPORTE_RENTABILIDAD", "REPORTE_COMISIONES",
                    "REPORTE_APARTADOS", "REPORTE_INGRESOS", "REPORTE_ACTIVIDAD", "REPORTE_PRECIOS",
                    // Auditoria
                    "AUDITORIA_VER", "AUDITORIA_EXPORTAR",
                    // Configuracion
                    "CONFIG_EMPRESA", "CONFIG_PRECIOS", "CONFIG_SISTEMA",
                    // Clientes
                    "CLIENTE_VER", "CLIENTE_CREAR", "CLIENTE_EDITAR", "CLIENTE_ELIMINAR",
                    // Planes de pago
                    "PLAN_PAGO_VER", "PLAN_PAGO_CREAR", "PLAN_PAGO_EDITAR", "PLAN_PAGO_ELIMINAR",
                    // Pagos
                    "PAGO_REGISTRAR", "PAGO_VER", "PAGO_ELIMINAR",
                    // Archivos
                    "ARCHIVO_CREAR", "ARCHIVO_VER", "ARCHIVO_ELIMINAR",
                    // Admin (special authority for audit endpoints)
                    "ADMIN"
            ).forEach(codigo -> permisoRepository.save(
                    com.inmobiliaria.terrenos.domain.entity.Permiso.builder()
                            .codigo(codigo)
                            .nombre(codigo)
                            .modulo(codigo.split("_")[0])
                            .build()));
        }
    }
}
