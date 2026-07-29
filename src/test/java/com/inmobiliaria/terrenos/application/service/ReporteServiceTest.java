package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.reporte.DashboardResponse;
import com.inmobiliaria.terrenos.application.dto.reporte.ProyectoEstadisticasResponse;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.enums.EstadoProyecto;
import com.inmobiliaria.terrenos.domain.repository.*;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private ApartadoRepository apartadoRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private ReporteService reporteService;

    private final Long tenantId = 1L;
    private Proyecto proyecto;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        proyecto = Proyecto.builder()
                .id(100L)
                .tenantId(tenantId)
                .nombre("Proyecto Vista Mar")
                .estadoProyecto(EstadoProyecto.EN_VENTA)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void obtenerDashboard_Exitoso() {
        when(proyectoRepository.findByTenantIdAndDeletedFalse(tenantId)).thenReturn(List.of(proyecto));
        when(terrenoRepository.findByTenantIdAndDeletedFalse(tenantId)).thenReturn(Collections.emptyList());
        when(cotizacionRepository.countByTenantIdAndDeletedFalse(tenantId)).thenReturn(5L);
        when(cotizacionRepository.findCotizacionesVigentes(eq(tenantId), any())).thenReturn(Collections.emptyList());
        when(apartadoRepository.countByTenantIdAndEstadoAndDeletedFalse(eq(tenantId), any())).thenReturn(2L);
        when(apartadoRepository.findApartadosVigentes(eq(tenantId), any())).thenReturn(Collections.emptyList());
        when(apartadoRepository.findApartadosVencidos(eq(tenantId), any())).thenReturn(Collections.emptyList());
        when(ventaRepository.countByTenantIdAndDeletedFalse(tenantId)).thenReturn(3L);
        when(ventaRepository.countByTenantIdAndEstadoAndDeletedFalse(eq(tenantId), any())).thenReturn(1L);
        when(ventaRepository.sumTotalVentas(tenantId)).thenReturn(BigDecimal.valueOf(300000));
        when(ventaRepository.sumTotalComisiones(tenantId)).thenReturn(BigDecimal.valueOf(15000));

        DashboardResponse dashboard = reporteService.obtenerDashboard();

        assertNotNull(dashboard);
        assertEquals(1, dashboard.getTotalProyectos());
        assertEquals(1, dashboard.getProyectosActivos());
        assertEquals(5, dashboard.getTotalCotizaciones());
        assertEquals(3, dashboard.getTotalVentas());
        assertEquals(BigDecimal.valueOf(300000), dashboard.getMontoTotalVentas());
    }

    @Test
    void obtenerEstadisticasPorProyecto_Exitoso() {
        when(proyectoRepository.findByTenantIdAndDeletedFalse(tenantId)).thenReturn(List.of(proyecto));
        when(terrenoRepository.findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(tenantId, 100L))
                .thenReturn(Collections.emptyList());

        List<ProyectoEstadisticasResponse> resultado = reporteService.obtenerEstadisticasPorProyecto();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Proyecto Vista Mar", resultado.get(0).getProyectoNombre());
    }
}
