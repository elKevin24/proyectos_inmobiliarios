package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.application.dto.pago.*;
import com.inmobiliaria.terrenos.domain.entity.*;
import com.inmobiliaria.terrenos.domain.enums.*;
import com.inmobiliaria.terrenos.domain.repository.*;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.interfaces.mapper.*;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PlanPagoRepository planPagoRepository;
    @Mock
    private AmortizacionRepository amortizacionRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private TerrenoRepository terrenoRepository;
    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PlanPagoMapper planPagoMapper;
    @Mock
    private AmortizacionMapper amortizacionMapper;
    @Mock
    private PagoMapper pagoMapper;

    @InjectMocks
    private PagoService pagoService;

    private final Long tenantId = 1L;
    private PlanPago planPago;
    private PlanPagoResponse planPagoResponse;
    private Venta venta;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        venta = Venta.builder()
                .id(200L)
                .tenantId(tenantId)
                .clienteId(500L)
                .build();

        planPago = PlanPago.builder()
                .id(100L)
                .tenantId(tenantId)
                .ventaId(200L)
                .clienteId(500L)
                .tipoPlan(TipoPlanPago.FINANCIAMIENTO_PROPIO)
                .numeroPagos(12)
                .frecuenciaPago(FrecuenciaPago.MENSUAL)
                .tasaInteresAnual(BigDecimal.valueOf(10.0))
                .montoTotal(BigDecimal.valueOf(150000.00))
                .enganche(BigDecimal.valueOf(30000.00))
                .montoFinanciado(BigDecimal.valueOf(120000.00))
                .diasGracia(0)
                .tasaMoraMensual(BigDecimal.ZERO)
                .fechaInicio(LocalDate.now())
                .fechaPrimerPago(LocalDate.now().plusMonths(1))
                .aplicaInteres(false)
                .deleted(false)
                .build();

        planPagoResponse = new PlanPagoResponse();
        planPagoResponse.setId(100L);
        planPagoResponse.setVentaId(200L);
        planPagoResponse.setTipoPlan(TipoPlanPago.FINANCIAMIENTO_PROPIO);

        lenient().when(amortizacionRepository.contarByPlanPagoId(any(), any())).thenReturn(0L);
        lenient().when(amortizacionRepository.contarByEstado(any(), any(), any())).thenReturn(0L);
        lenient().when(amortizacionRepository.sumarTotalPagado(any(), any())).thenReturn(BigDecimal.ZERO);
        lenient().when(amortizacionRepository.sumarTotalPendiente(any(), any())).thenReturn(BigDecimal.ZERO);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void crearPlanPago_Exitoso() {
        CreatePlanPagoRequest request = CreatePlanPagoRequest.builder()
                .ventaId(200L)
                .tipoPlan(TipoPlanPago.FINANCIAMIENTO_PROPIO)
                .numeroPagos(12)
                .frecuenciaPago(FrecuenciaPago.MENSUAL)
                .fechaPrimerPago(LocalDate.now().plusMonths(1))
                .tasaInteresAnual(BigDecimal.valueOf(10.0))
                .build();

        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(200L, tenantId)).thenReturn(Optional.of(venta));
        when(planPagoRepository.existePlanParaVenta(tenantId, 200L)).thenReturn(false);
        when(planPagoMapper.toEntity(request)).thenReturn(planPago);
        when(planPagoRepository.save(planPago)).thenReturn(planPago);
        when(planPagoMapper.toResponse(planPago)).thenReturn(planPagoResponse);

        PlanPagoResponse resultado = pagoService.crearPlanPago(request);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        verify(planPagoRepository, times(1)).save(planPago);
    }

    @Test
    void crearPlanPago_YaExistePlan_LanzaExcepcion() {
        CreatePlanPagoRequest request = CreatePlanPagoRequest.builder()
                .ventaId(200L)
                .build();

        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(200L, tenantId)).thenReturn(Optional.of(venta));
        when(planPagoRepository.existePlanParaVenta(tenantId, 200L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> pagoService.crearPlanPago(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(planPagoRepository, never()).save(any());
    }

    @Test
    void obtenerPlanPago_Existente_Exitoso() {
        when(planPagoRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId)).thenReturn(Optional.of(planPago));
        when(planPagoMapper.toResponse(planPago)).thenReturn(planPagoResponse);

        PlanPagoResponse resultado = pagoService.obtenerPlanPago(100L);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
    }

    @Test
    void obtenerPlanPago_Inexistente_LanzaExcepcion() {
        when(planPagoRepository.findByIdAndTenantIdAndDeletedFalse(999L, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.obtenerPlanPago(999L));
    }

    @Test
    void aplicarPago_NoCuotasPendientes_LanzaExcepcion() {
        CreatePagoRequest request = CreatePagoRequest.builder()
                .planPagoId(100L)
                .montoPagado(BigDecimal.valueOf(5000))
                .fechaPago(LocalDate.now())
                .build();

        Pago mockPago = new Pago();

        when(planPagoRepository.findByIdAndTenantIdAndDeletedFalse(100L, tenantId)).thenReturn(Optional.of(planPago));
        when(pagoMapper.toEntity(request)).thenReturn(mockPago);
        when(amortizacionRepository.findVencidasHastaFecha(eq(tenantId), eq(100L), any())).thenReturn(new ArrayList<>());
        when(amortizacionRepository.findPendientesByPlanPagoId(tenantId, 100L)).thenReturn(new ArrayList<>());

        BusinessException exception = assertThrows(BusinessException.class, () -> pagoService.aplicarPago(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(pagoRepository, never()).save(any());
    }
}
