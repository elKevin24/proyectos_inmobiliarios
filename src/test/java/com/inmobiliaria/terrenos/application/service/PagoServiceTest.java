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

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private Venta venta;
    private PlanPagoResponse planPagoResponse;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);

        venta = Venta.builder()
                .id(50L)
                .tenantId(tenantId)
                .clienteId(10L)
                .terrenoId(200L)
                .build();

        planPago = PlanPago.builder()
                .id(1L)
                .tenantId(tenantId)
                .ventaId(50L)
                .clienteId(10L)
                .montoTotal(BigDecimal.valueOf(100000))
                .enganche(BigDecimal.valueOf(20000))
                .montoFinanciado(BigDecimal.valueOf(80000))
                .numeroPagos(10)
                .tasaInteresAnual(BigDecimal.ZERO)
                .tasaInteresMensual(BigDecimal.ZERO)
                .frecuenciaPago(FrecuenciaPago.MENSUAL)
                .fechaPrimerPago(LocalDate.now().plusDays(30))
                .aplicaInteres(false)
                .diasGracia(5)
                .tasaMoraMensual(BigDecimal.valueOf(2.0))
                .build();

        planPagoResponse = new PlanPagoResponse();
        planPagoResponse.setId(1L);
        planPagoResponse.setMontoFinanciado(BigDecimal.valueOf(80000));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void crearPlanPago_VentaNoExiste_LanzaExcepcion() {
        CreatePlanPagoRequest request = new CreatePlanPagoRequest();
        request.setVentaId(99L);

        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(99L, tenantId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.crearPlanPago(request));
    }

    @Test
    void crearPlanPago_YaExistePlan_LanzaExcepcion() {
        CreatePlanPagoRequest request = new CreatePlanPagoRequest();
        request.setVentaId(50L);

        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(venta));
        when(planPagoRepository.existePlanParaVenta(tenantId, 50L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> pagoService.crearPlanPago(request));
        assertTrue(exception.getMessage().contains("Ya existe un plan de pago"));
    }

    @Test
    void crearPlanPago_Exitoso() {
        CreatePlanPagoRequest request = new CreatePlanPagoRequest();
        request.setVentaId(50L);
        request.setFechaPrimerPago(LocalDate.now().plusDays(30));
        request.setNumeroPagos(10);
        request.setFrecuenciaPago(FrecuenciaPago.MENSUAL);

        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(50L, tenantId)).thenReturn(Optional.of(venta));
        when(planPagoRepository.existePlanParaVenta(tenantId, 50L)).thenReturn(false);
        when(planPagoMapper.toEntity(request)).thenReturn(planPago);
        when(planPagoRepository.save(any())).thenReturn(planPago);
        when(planPagoMapper.toResponse(planPago)).thenReturn(planPagoResponse);
        when(amortizacionRepository.contarByPlanPagoId(tenantId, 1L)).thenReturn(10L);
        when(amortizacionRepository.contarByEstado(any(), any(), any())).thenReturn(0L);
        when(amortizacionRepository.sumarTotalPagado(tenantId, 1L)).thenReturn(BigDecimal.ZERO);
        when(amortizacionRepository.sumarTotalPendiente(tenantId, 1L)).thenReturn(BigDecimal.valueOf(80000));

        PlanPagoResponse response = pagoService.crearPlanPago(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(amortizacionRepository, times(1)).saveAll(any());
    }

    @Test
    void aplicarPago_SinAmortizacionesPendientes_LanzaExcepcion() {
        CreatePagoRequest request = new CreatePagoRequest();
        request.setPlanPagoId(1L);
        request.setMontoPagado(BigDecimal.valueOf(5000));
        request.setFechaPago(LocalDate.now());

        when(pagoMapper.toEntity(any())).thenReturn(new Pago());
        when(planPagoRepository.findByIdAndTenantIdAndDeletedFalse(1L, tenantId)).thenReturn(Optional.of(planPago));
        when(amortizacionRepository.findVencidasHastaFecha(tenantId, 1L, LocalDate.now())).thenReturn(List.of());
        when(amortizacionRepository.findPendientesByPlanPagoId(tenantId, 1L)).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> pagoService.aplicarPago(request));
        assertTrue(exception.getMessage().contains("No hay cuotas pendientes"));
    }

    @Test
    void aplicarPago_Exitoso() {
        CreatePagoRequest request = new CreatePagoRequest();
        request.setPlanPagoId(1L);
        request.setMontoPagado(BigDecimal.valueOf(8000));
        request.setFechaPago(LocalDate.now());

        Amortizacion cuota1 = Amortizacion.builder()
                .id(100L)
                .tenantId(tenantId)
                .planPagoId(1L)
                .numeroCuota(1)
                .montoCuota(BigDecimal.valueOf(8000))
                .montoPagado(BigDecimal.ZERO)
                .montoPendiente(BigDecimal.valueOf(8000))
                .capital(BigDecimal.valueOf(8000))
                .interes(BigDecimal.ZERO)
                .moraAcumulada(BigDecimal.ZERO)
                .estado(EstadoAmortizacion.PENDIENTE)
                .build();

        Pago pago = Pago.builder().id(10L).montoPagado(BigDecimal.valueOf(8000)).build();
        PagoResponse pagoResponse = new PagoResponse();
        pagoResponse.setId(10L);

        when(planPagoRepository.findByIdAndTenantIdAndDeletedFalse(1L, tenantId)).thenReturn(Optional.of(planPago));
        when(amortizacionRepository.findVencidasHastaFecha(tenantId, 1L, LocalDate.now())).thenReturn(List.of());
        when(amortizacionRepository.findPendientesByPlanPagoId(tenantId, 1L)).thenReturn(List.of(cuota1));
        when(pagoMapper.toEntity(request)).thenReturn(pago);
        when(pagoRepository.save(any())).thenReturn(pago);
        when(pagoMapper.toResponse(pago)).thenReturn(pagoResponse);

        PagoResponse response = pagoService.aplicarPago(request);

        assertNotNull(response);
        assertEquals(EstadoAmortizacion.PAGADO, cuota1.getEstado());
        verify(pagoRepository, times(1)).save(any());
        verify(amortizacionRepository, times(1)).save(cuota1);
    }
}
