package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.auditoria.AuditFiltrosRequest;
import com.inmobiliaria.terrenos.application.dto.auditoria.AuditLogCriticaResponse;
import com.inmobiliaria.terrenos.application.dto.auditoria.AuditLogSimpleResponse;
import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogCritica;
import com.inmobiliaria.terrenos.domain.entity.audit.AuditLogSimple;
import com.inmobiliaria.terrenos.domain.enums.TipoAccionAudit;
import com.inmobiliaria.terrenos.domain.enums.TipoOperacionAudit;
import com.inmobiliaria.terrenos.domain.repository.AuditLogArchiveRepository;
import com.inmobiliaria.terrenos.domain.repository.AuditLogCriticaRepository;
import com.inmobiliaria.terrenos.domain.repository.AuditLogSimpleRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogSimpleRepository auditLogSimpleRepository;

    @Mock
    private AuditLogCriticaRepository auditLogCriticaRepository;

    @Mock
    private AuditLogArchiveRepository auditLogArchiveRepository;

    @InjectMocks
    private AuditService auditService;

    private final Long tenantId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void registrarAccionSimple_Exitoso() {
        auditService.registrarAccionSimple(TipoAccionAudit.LOGIN, "Acceso al sistema", null);

        verify(auditLogSimpleRepository, times(1)).save(any(AuditLogSimple.class));
    }

    @Test
    void registrarCambioCritico_SinCambio_NoGuarda() {
        auditService.registrarCambioCritico("proyectos", 100L, "nombre", "MismoNombre", "MismoNombre", TipoOperacionAudit.UPDATE, "Ninguno");

        verify(auditLogCriticaRepository, never()).save(any());
    }

    @Test
    void registrarCambioCritico_ConCambio_Guarda() {
        auditService.registrarCambioCritico("proyectos", 100L, "nombre", "NombreViejo", "NombreNuevo", TipoOperacionAudit.UPDATE, "Actualización");

        verify(auditLogCriticaRepository, times(1)).save(any(AuditLogCritica.class));
    }

    @Test
    void obtenerLogsSimples_Exitoso() {
        AuditLogSimple logSimple = AuditLogSimple.builder()
                .id(1L)
                .tenantId(tenantId)
                .usuarioEmail("admin@test.com")
                .tipoAccion(TipoAccionAudit.LOGIN)
                .accion(TipoAccionAudit.LOGIN.name())
                .descripcion("Login")
                .fecha(LocalDateTime.now())
                .build();

        when(auditLogSimpleRepository.findByRangoFechas(eq(tenantId), any(), any()))
                .thenReturn(List.of(logSimple));

        AuditFiltrosRequest request = new AuditFiltrosRequest();
        List<AuditLogSimpleResponse> resultado = auditService.obtenerLogsSimples(request);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("admin@test.com", resultado.get(0).getUsuarioEmail());
    }

    @Test
    void obtenerHistorialRegistro_Exitoso() {
        AuditLogCritica logCritica = AuditLogCritica.builder()
                .id(1L)
                .tenantId(tenantId)
                .tabla("terrenos")
                .registroId(200L)
                .campo("precio")
                .valorAnterior("100000")
                .valorNuevo("120000")
                .tipoOperacion(TipoOperacionAudit.UPDATE)
                .operacion(TipoOperacionAudit.UPDATE.name())
                .fecha(LocalDateTime.now())
                .build();

        when(auditLogCriticaRepository.findByTablaYRegistro(tenantId, "terrenos", 200L))
                .thenReturn(List.of(logCritica));

        List<AuditLogCriticaResponse> resultado = auditService.obtenerHistorialRegistro("terrenos", 200L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("100000", resultado.get(0).getValorAnterior());
    }

    @Test
    void archivarLogsAntiguos_Exitoso() {
        when(auditLogSimpleRepository.findAntiguos(eq(tenantId), any())).thenReturn(Collections.emptyList());
        when(auditLogCriticaRepository.findAntiguos(eq(tenantId), any())).thenReturn(Collections.emptyList());
        when(auditLogSimpleRepository.deleteAntiguos(eq(tenantId), any())).thenReturn(0);
        when(auditLogCriticaRepository.deleteAntiguos(eq(tenantId), any())).thenReturn(0);

        int archivados = auditService.archivarLogsAntiguos();

        assertEquals(0, archivados);
    }
}
