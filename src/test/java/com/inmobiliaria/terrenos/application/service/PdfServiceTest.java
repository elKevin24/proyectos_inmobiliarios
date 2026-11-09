package com.inmobiliaria.terrenos.application.service;

import com.inmobiliaria.terrenos.domain.entity.*;
import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import com.inmobiliaria.terrenos.domain.enums.EstadoVenta;
import com.inmobiliaria.terrenos.domain.enums.FrecuenciaPago;
import com.inmobiliaria.terrenos.domain.repository.*;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private TerrenoRepository terrenoRepository;

    @Mock
    private ProyectoRepository proyectoRepository;

    @Mock
    private PlanPagoRepository planPagoRepository;

    @Mock
    private AmortizacionRepository amortizacionRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private PdfService pdfService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Generar PDF de Contrato de Venta Exitoso")
    void testGenerarContratoVentaPdf_Exito() {
        Venta venta = Venta.builder()
                .id(10L)
                .tenantId(1L)
                .terrenoId(5L)
                .folio("VEN-00010")
                .fechaVenta(LocalDate.now())
                .compradorNombre("Juan Pérez")
                .compradorRfc("PERJ850101XYZ")
                .precioTotal(new BigDecimal("500000.00"))
                .montoApartadoAcreditado(new BigDecimal("50000.00"))
                .montoFinal(new BigDecimal("450000.00"))
                .formaPago("FINANCIADO")
                .estado(EstadoVenta.PAGADO)
                .build();

        Terreno terreno = Terreno.builder()
                .id(5L)
                .proyectoId(2L)
                .numeroLote("L-15")
                .manzana("M-03")
                .area(new BigDecimal("250.00"))
                .build();

        Proyecto proyecto = Proyecto.builder()
                .id(2L)
                .nombre("Residencial Las Palmas")
                .build();

        Tenant tenant = Tenant.builder()
                .nombre("Inmobiliaria Ejemplo")
                .build();
        tenant.setId(1L);

        when(ventaRepository.findByIdAndTenantIdAndDeletedFalse(10L, 1L)).thenReturn(Optional.of(venta));
        when(terrenoRepository.findById(5L)).thenReturn(Optional.of(terreno));
        when(proyectoRepository.findById(2L)).thenReturn(Optional.of(proyecto));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        byte[] pdfBytes = pdfService.generarContratoVentaPdf(10L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    @DisplayName("Generar PDF de Estado de Cuenta Exitoso")
    void testGenerarEstadoCuentaPdf_Exito() {
        PlanPago planPago = PlanPago.builder()
                .id(3L)
                .tenantId(1L)
                .ventaId(10L)
                .montoTotal(new BigDecimal("500000.00"))
                .montoFinanciado(new BigDecimal("450000.00"))
                .plazoMeses(12)
                .numeroPagos(12)
                .frecuenciaPago(FrecuenciaPago.MENSUAL)
                .tasaInteresAnual(new BigDecimal("12.00"))
                .build();

        Amortizacion cuota1 = Amortizacion.builder()
                .id(100L)
                .numeroCuota(1)
                .fechaVencimiento(LocalDate.now().plusMonths(1))
                .capital(new BigDecimal("35000.00"))
                .interes(new BigDecimal("5000.00"))
                .montoCuota(new BigDecimal("40000.00"))
                .montoPagado(new BigDecimal("40000.00"))
                .estado(EstadoAmortizacion.PAGADO)
                .build();

        Tenant tenant = Tenant.builder()
                .nombre("Inmobiliaria Ejemplo")
                .build();
        tenant.setId(1L);

        when(planPagoRepository.findByIdAndTenantIdAndDeletedFalse(3L, 1L)).thenReturn(Optional.of(planPago));
        when(amortizacionRepository.findByPlanPagoId(1L, 3L))
                .thenReturn(List.of(cuota1));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));

        byte[] pdfBytes = pdfService.generarEstadoCuentaPdf(3L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
