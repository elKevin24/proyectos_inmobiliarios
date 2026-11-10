package com.inmobiliaria.terrenos.infrastructure.cv;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacén en memoria de SSE Emitters por tareaId.
 * Incluye caché de eventos terminales ("completado" / "error") para evitar condiciones de carrera
 * cuando el procesamiento finaliza antes de que el cliente abra la conexión EventSource.
 *
 * @author Kevin
 * @version 1.1.0
 */
@Component
public class PlanoStatusStore {

    public record TerminalEvent(String evento, String data) {}

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, TerminalEvent> terminalEvents = new ConcurrentHashMap<>();
    private final Map<String, Long> tareaTenants = new ConcurrentHashMap<>();

    public void registrarTareaTenant(String tareaId, Long tenantId) {
        if (tareaId != null && tenantId != null) {
            tareaTenants.put(tareaId, tenantId);
        }
    }

    public void verificarAccesoTarea(String tareaId, Long tenantId) {
        if (tareaId != null && tareaTenants.containsKey(tareaId)) {
            Long ownerTenant = tareaTenants.get(tareaId);
            if (ownerTenant != null && !ownerTenant.equals(tenantId)) {
                throw new AccessDeniedException("No tiene acceso a esta tarea");
            }
        }
    }

    public void verificarAccesoTarea(Long tareaId, Long tenantId) {
        if (tareaId != null) {
            verificarAccesoTarea(String.valueOf(tareaId), tenantId);
        }
    }

    public void registrar(String tareaId, SseEmitter emitter) {
        TerminalEvent cached = terminalEvents.remove(tareaId);
        if (cached != null) {
            try {
                emitter.send(SseEmitter.event().name(cached.evento()).data(cached.data()));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return;
        }

        emitters.put(tareaId, emitter);
        emitter.onCompletion(() -> emitters.remove(tareaId));
        emitter.onTimeout(() -> emitters.remove(tareaId));
    }

    /**
     * Envía un evento de estado al cliente suscrito o almacena el evento terminal si aún no se ha suscrito.
     */
    public void notificar(String tareaId, String evento, String data) {
        boolean isTerminal = "completado".equals(evento) || "error".equals(evento);
        SseEmitter emitter = emitters.get(tareaId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(evento).data(data));
                if (isTerminal) {
                    emitter.complete();
                    emitters.remove(tareaId);
                }
            } catch (Exception e) {
                emitters.remove(tareaId);
                emitter.completeWithError(e);
            }
        } else if (isTerminal) {
            terminalEvents.put(tareaId, new TerminalEvent(evento, data));
        }
    }

    public boolean existe(String tareaId) {
        return emitters.containsKey(tareaId) || terminalEvents.containsKey(tareaId);
    }
}
