package com.inmobiliaria.terrenos.infrastructure.pdf;

import com.inmobiliaria.terrenos.application.dto.pdf.ContratoVentaPdfData;
import com.inmobiliaria.terrenos.application.dto.pdf.EstadoCuentaPdfData;
import com.inmobiliaria.terrenos.domain.enums.EstadoAmortizacion;
import com.inmobiliaria.terrenos.domain.enums.FrecuenciaPago;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorPdfDocumentoAdapterTest {

    private GeneradorPdfDocumentoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GeneradorPdfDocumentoAdapter();
    }

    @Test
    @DisplayName("Generar Contrato de Venta PDF exitosamente")
    void testGenerarContratoVenta() {
        ContratoVentaPdfData data = ContratoVentaPdfData.builder()
                .nombreEmpresa("Inmobiliaria Bosque")
                .folioVenta("FOL-12345")
                .fechaVenta(LocalDate.now())
                .compradorNombre("Carlos Slim")
                .compradorRfc("SLIM500101XYZ")
                .compradorCurp("SLIM500101HDFR01")
                .compradorTelefono("555-1234567")
                .compradorEmail("carlos@example.com")
                .compradorDireccion("Av. Reforma 123")
                .proyectoNombre("Bosque Real")
                .numeroLote("A-12")
                .manzana("M-01")
                .areaTerreno(new BigDecimal("350.00"))
                .precioTotal(new BigDecimal("1200000.00"))
                .montoApartadoAcreditado(new BigDecimal("100000.00"))
                .montoFinal(new BigDecimal("1100000.00"))
                .formaPago("TRANSFERENCIA")
                .estadoVenta("COMPLETADO")
                .build();

        byte[] pdf = adapter.generarContratoVenta(data);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    @DisplayName("Generar Estado de Cuenta PDF exitosamente")
    void testGenerarEstadoCuenta() {
        EstadoCuentaPdfData.CuotaItem cuota = EstadoCuentaPdfData.CuotaItem.builder()
                .numeroCuota(1)
                .fechaVencimiento(LocalDate.now().plusMonths(1))
                .capital(new BigDecimal("10000.00"))
                .interes(new BigDecimal("1500.00"))
                .montoCuota(new BigDecimal("11500.00"))
                .montoPagado(new BigDecimal("11500.00"))
                .estado(EstadoAmortizacion.PAGADO)
                .build();

        EstadoCuentaPdfData data = EstadoCuentaPdfData.builder()
                .nombreEmpresa("Inmobiliaria Bosque")
                .planPagoId(10L)
                .ventaId(20L)
                .montoFinanciado(new BigDecimal("500000.00"))
                .plazoMeses(24)
                .numeroPagos(24)
                .frecuenciaPago(FrecuenciaPago.MENSUAL)
                .tasaInteresAnual(new BigDecimal("12.5"))
                .montoTotal(new BigDecimal("620000.00"))
                .cuotas(List.of(cuota))
                .build();

        byte[] pdf = adapter.generarEstadoCuenta(data);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
