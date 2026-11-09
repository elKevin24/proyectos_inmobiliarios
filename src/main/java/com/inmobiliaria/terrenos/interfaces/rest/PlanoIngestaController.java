package com.inmobiliaria.terrenos.interfaces.rest;

import com.inmobiliaria.terrenos.application.dto.plano.ConfirmarIngestaRequest;
import com.inmobiliaria.terrenos.application.service.PlanoIngestaService;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * Controlador REST para el módulo de ingesta automática de planos.
 *
 * @author Kevin
 * @version 2.1.0
 */
@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/planos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Plano Ingesta CV", description = "Ingesta automática de planos con OpenCV + OCR y progreso SSE")
@SecurityRequirement(name = "bearerAuth")
public class PlanoIngestaController {

    private final PlanoIngestaService planoIngestaService;
    private final PlanoStatusStore statusStore;

    @PostMapping("/analizar")
    @PreAuthorize("hasAnyAuthority('ARCHIVO_CREAR', 'TERRENO_CREAR', 'ADMIN')")
    @Operation(summary = "Iniciar análisis de plano con IA/CV", description = "Inicia el análisis asíncrono del plano subido y retorna tareaId + sseUrl")
    public ResponseEntity<Map<String, String>> analizarPlano(
            @PathVariable Long proyectoId,
            @RequestBody Map<String, String> body) throws IOException {

        String nombreAlmacenado = body.get("nombreAlmacenado");
        if (nombreAlmacenado == null || nombreAlmacenado.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "nombreAlmacenado es requerido"));
        }

        String tareaId = planoIngestaService.iniciarAnalisisPorNombre(proyectoId, nombreAlmacenado);

        log.info("Análisis iniciado para proyecto {}. TareaId: {}", proyectoId, tareaId);

        return ResponseEntity
                .accepted()
                .body(Map.of(
                        "tareaId", tareaId,
                        "mensaje", "El análisis del plano ha comenzado.",
                        "sseUrl", "/api/v1/proyectos/" + proyectoId + "/planos/estado/" + tareaId
                ));
    }

    @GetMapping(value = "/estado/{tareaId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('ARCHIVO_VER', 'TERRENO_VER', 'PROYECTO_VER', 'ADMIN')")
    @Operation(summary = "Suscribirse al estado SSE del análisis", description = "Canal Server-Sent Events con eventos: progreso, completado, error")
    public SseEmitter estadoAnalisis(
            @PathVariable Long proyectoId,
            @PathVariable String tareaId) {

        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        statusStore.registrar(tareaId, emitter);

        log.info("Cliente suscrito a estado de tarea: {}", tareaId);
        return emitter;
    }

    @PostMapping("/confirmar")
    @PreAuthorize("hasAnyAuthority('TERRENO_CREAR', 'ADMIN')")
    @Operation(summary = "Confirmar e ingestar lotes validados", description = "Persiste los lotes validados por el usuario en el proyecto y actualiza contadores")
    public ResponseEntity<Map<String, Object>> confirmarIngesta(
            @PathVariable Long proyectoId,
            @RequestBody ConfirmarIngestaRequest request) {

        request.setProyectoId(proyectoId);
        planoIngestaService.confirmarIngesta(request);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Lotes ingresados exitosamente con estado DISPONIBLE",
                "totalLotes", request.getLotes().size()
        ));
    }
}
