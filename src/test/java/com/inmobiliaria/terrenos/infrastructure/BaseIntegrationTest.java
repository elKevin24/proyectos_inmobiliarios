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
            permisoRepository.save(com.inmobiliaria.terrenos.domain.entity.Permiso.builder()
                    .codigo("PROYECTO_CREAR")
                    .nombre("Crear Proyecto")
                    .modulo("PROYECTO")
                    .build());
            permisoRepository.save(com.inmobiliaria.terrenos.domain.entity.Permiso.builder()
                    .codigo("PROYECTO_VER")
                    .nombre("Ver Proyecto")
                    .modulo("PROYECTO")
                    .build());
            permisoRepository.save(com.inmobiliaria.terrenos.domain.entity.Permiso.builder()
                    .codigo("PROYECTO_EDITAR")
                    .nombre("Editar Proyecto")
                    .modulo("PROYECTO")
                    .build());
            permisoRepository.save(com.inmobiliaria.terrenos.domain.entity.Permiso.builder()
                    .codigo("PROYECTO_ELIMINAR")
                    .nombre("Eliminar Proyecto")
                    .modulo("PROYECTO")
                    .build());
        }
    }
}
