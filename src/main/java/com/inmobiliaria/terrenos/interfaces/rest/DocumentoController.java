package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para descarga y visualización de documentos oficiales en PDF.
 *
 * @author Dev A (Lead Backend & Architect)
 * @version 1.1.0
 */
@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Documentos PDF", description = "Endpoints para la descarga de Contratos y Estados de Cuenta en PDF")
public class DocumentoController {

    private final PdfService pdfService;

    @GetMapping("/contrato-venta/{ventaId}")
    @PreAuthorize("hasAuthority('VENTA_VER')")
    @Operation(summary = "Descargar Contrato de Compraventa en PDF", description = "Genera y descarga el PDF del contrato privado de compraventa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<byte[]> descargarContratoVenta(@PathVariable Long ventaId) {
        log.info("GET /api/v1/documentos/contrato-venta/{}", ventaId);

        byte[] pdfBytes = pdfService.generarContratoVentaPdf(ventaId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "contrato-venta-" + ventaId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/estado-cuenta/{planPagoId}")
    @PreAuthorize("hasAuthority('PLAN_PAGO_VER')")
    @Operation(summary = "Descargar Estado de Cuenta en PDF", description = "Genera y descarga el PDF del estado de cuenta y amortización")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Plan de pago no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<byte[]> descargarEstadoCuenta(@PathVariable Long planPagoId) {
        log.info("GET /api/v1/documentos/estado-cuenta/{}", planPagoId);

        byte[] pdfBytes = pdfService.generarEstadoCuentaPdf(planPagoId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "estado-cuenta-plan-" + planPagoId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
