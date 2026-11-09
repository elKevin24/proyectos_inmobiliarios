package com.inmobiliaria.terrenos.infrastructure.tenant;

import com.inmobiliaria.terrenos.application.dto.proyecto.CreateProyectoRequest;
import com.inmobiliaria.terrenos.application.dto.proyecto.ProyectoResponse;
import com.inmobiliaria.terrenos.application.dto.terreno.CreateTerrenoRequest;
import com.inmobiliaria.terrenos.application.dto.terreno.TerrenoResponse;
import com.inmobiliaria.terrenos.application.service.PlanoService;
import com.inmobiliaria.terrenos.application.service.ProyectoService;
import com.inmobiliaria.terrenos.application.service.TerrenoService;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import com.inmobiliaria.terrenos.infrastructure.BaseIntegrationTest;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MultiTenancyIsolationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProyectoService proyectoService;

    @Autowired
    private TerrenoService terrenoService;

    @Autowired
    private PlanoService planoService;

    private static final Long TENANT_A = 1001L;
    private static final Long TENANT_B = 2002L;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Aislamiento Multi-Tenant - Tenant B no puede ver ni listar proyectos creados por Tenant A")
    void testCrossTenantProyectoIsolation() {
        // 1. Crear proyecto como Tenant A
        TenantContext.setTenantId(TENANT_A);
        CreateProyectoRequest reqA = new CreateProyectoRequest();
        reqA.setNombre("Residencial Tenant A");
        reqA.setDireccion("Av. A 123");
        reqA.setEstadoProyecto(EstadoProyecto.EN_VENTA);

        ProyectoResponse respA = proyectoService.crearProyecto(reqA);
        Long idProyectoA = respA.getId();

        // 2. Cambiar contexto a Tenant B
        TenantContext.setTenantId(TENANT_B);

        // Tenant B busca proyectos -> no debe ver el proyecto de Tenant A
        List<ProyectoResponse> listaTenantB = proyectoService.listarProyectos();
        assertThat(listaTenantB).noneMatch(p -> p.getId().equals(idProyectoA));

        // Tenant B intenta acceder directamente por ID -> debe lanzar ResourceNotFoundException (aislamiento ciego)
        assertThatThrownBy(() -> proyectoService.obtenerProyecto(idProyectoA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proyecto no encontrado");
    }

    @Test
    @DisplayName("Aislamiento Multi-Tenant - Tenant B no puede consultar ni modificar terrenos de Tenant A")
    void testCrossTenantTerrenoIsolation() {
        // 1. Crear proyecto y terreno como Tenant A
        TenantContext.setTenantId(TENANT_A);
        CreateProyectoRequest reqProj = new CreateProyectoRequest();
        reqProj.setNombre("Condominio Sol Tenant A");
        reqProj.setDireccion("Calle 1");
        ProyectoResponse projA = proyectoService.crearProyecto(reqProj);

        CreateTerrenoRequest reqTerreno = new CreateTerrenoRequest();
        reqTerreno.setProyectoId(projA.getId());
        reqTerreno.setNumeroLote("LOTE-TENANT-A-01");
        reqTerreno.setArea(new BigDecimal("300.00"));
        reqTerreno.setPrecioBase(new BigDecimal("150000.00"));

        TerrenoResponse terrenoA = terrenoService.crearTerreno(reqTerreno);
        Long idTerrenoA = terrenoA.getId();

        // 2. Cambiar contexto a Tenant B
        TenantContext.setTenantId(TENANT_B);

        // Tenant B intenta obtener el terreno de Tenant A -> Excepción ResourceNotFoundException
        assertThatThrownBy(() -> terrenoService.obtenerTerreno(idTerrenoA))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Aislamiento Multi-Tenant - Tenant B no puede obtener el plano interactivo de un proyecto de Tenant A")
    void testCrossTenantPlanoInteractivoIsolation() {
        // 1. Crear proyecto como Tenant A
        TenantContext.setTenantId(TENANT_A);
        CreateProyectoRequest reqProj = new CreateProyectoRequest();
        reqProj.setNombre("Plaza Central Tenant A");
        reqProj.setDireccion("Centro");
        ProyectoResponse projA = proyectoService.crearProyecto(reqProj);

        // 2. Cambiar contexto a Tenant B
        TenantContext.setTenantId(TENANT_B);

        // Tenant B intenta obtener el plano del proyecto de Tenant A
        assertThatThrownBy(() -> planoService.obtenerPlanoInteractivo(projA.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proyecto no encontrado");
    }
}
