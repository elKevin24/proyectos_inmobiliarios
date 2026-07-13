import com.inmobiliaria.terrenos.application.dto.plano.ConfirmarIngestaRequest;
import com.inmobiliaria.terrenos.application.service.PlanoIngestaService;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * Controlador REST para el módulo de ingesta automática de planos.
 *
 * Flujo SaaS:
 * 1. POST /analizar  → retorna tareaId inmediatamente (HTTP 202)
 * 2. GET  /estado    → el cliente se suscribe vía SSE y recibe progreso en tiempo real
 * 3. POST /confirmar → una vez validado, persiste los lotes en BD
 *
 * @author Kevin
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/v1/proyectos/{proyectoId}/planos")
@RequiredArgsConstructor
@Slf4j
public class PlanoIngestaController {

    private final PlanoIngestaService planoIngestaService;
    private final PlanoStatusStore statusStore;

    /**
     * Inicia el análisis del plano ya subido al servidor.
     * Recibe el nombre almacenado del archivo (ya existe en /uploads) y
     * retorna un tareaId para que el cliente se suscriba al canal SSE.
     */
    @PostMapping("/analizar")
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

    /**
     * Canal SSE de estado del análisis.
     * El cliente escucha eventos: "progreso", "completado", "error"
     *
     * Timeout: 5 minutos (planos muy grandes de alta resolución)
     */
    @GetMapping(value = "/estado/{tareaId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter estadoAnalisis(
            @PathVariable Long proyectoId,
            @PathVariable String tareaId) {

        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5 minutos
        statusStore.registrar(tareaId, emitter);

        log.info("Cliente suscrito a estado de tarea: {}", tareaId);
        return emitter;
    }

    /**
     * Confirma e ingesta los lotes validados en la base de datos.
     * Llamado por el frontend tras la revisión manual del administrador.
     */
    @PostMapping("/confirmar")
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
