package com.inmobiliaria.terrenos.infrastructure.cv;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacén en memoria de SSE Emitters por tareaId.
 * Permite notificar al cliente en tiempo real el estado del procesamiento del plano.
 *
 * Principio SRP: responsabilidad única de gestionar el canal de notificación.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Component
public class PlanoStatusStore {

    /** Mapa concurrente: seguro para entornos multi-hilo (SaaS multi-tenant) */
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void registrar(String tareaId, SseEmitter emitter) {
        emitters.put(tareaId, emitter);
        emitter.onCompletion(() -> emitters.remove(tareaId));
        emitter.onTimeout(() -> emitters.remove(tareaId));
    }

    /**
     * Envía un evento de estado al cliente suscrito.
     * @param tareaId  ID de la tarea de procesamiento
     * @param evento   Nombre del evento (ej. "progreso", "completado", "error")
     * @param data     Datos a enviar como string (JSON o texto simple)
     */
    public void notificar(String tareaId, String evento, String data) {
        SseEmitter emitter = emitters.get(tareaId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(evento).data(data));
                if ("completado".equals(evento) || "error".equals(evento)) {
                    emitter.complete();
                    emitters.remove(tareaId);
                }
            } catch (Exception e) {
                emitters.remove(tareaId);
                emitter.completeWithError(e);
            }
        }
    }

    public boolean existe(String tareaId) {
        return emitters.containsKey(tareaId);
    }
}
