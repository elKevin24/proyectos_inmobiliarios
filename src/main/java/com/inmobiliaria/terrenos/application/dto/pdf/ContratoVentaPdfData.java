package com.inmobiliaria.terrenos.application.dto.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de datos desacoplado para la renderización gráfica del Contrato de Compraventa (SRP).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoVentaPdfData {
    private String nombreEmpresa;
    private String folioVenta;
    private LocalDate fechaVenta;
    private String compradorNombre;
    private String compradorRfc;
    private String compradorCurp;
    private String compradorTelefono;
    private String compradorEmail;
    private String compradorDireccion;
    private String proyectoNombre;
    private String numeroLote;
    private String manzana;
    private BigDecimal areaTerreno;
    private BigDecimal precioTotal;
    private BigDecimal montoApartadoAcreditado;
    private BigDecimal montoFinal;
    private String formaPago;
    private String estadoVenta;
}
