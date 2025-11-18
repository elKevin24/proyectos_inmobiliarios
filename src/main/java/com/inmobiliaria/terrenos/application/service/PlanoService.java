package com.inmobiliaria.terrenos.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.plano.CoordenadasPlano;
import com.inmobiliaria.terrenos.application.dto.plano.PlanoInteractivoResponse;
import com.inmobiliaria.terrenos.application.dto.plano.TerrenoVisualizacionResponse;
import com.inmobiliaria.terrenos.domain.entity.Archivo;
import com.inmobiliaria.terrenos.domain.entity.Fase;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.enums.TipoArchivo;
import com.inmobiliaria.terrenos.domain.repository.ArchivoRepository;
import com.inmobiliaria.terrenos.domain.repository.FaseRepository;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de planos interactivos
 *
 * @author Kevin
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanoService {

    private final ProyectoRepository proyectoRepository;
    private final TerrenoRepository terrenoRepository;
    private final ArchivoRepository archivoRepository;
    private final FaseRepository faseRepository;
    private final ObjectMapper objectMapper;

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    /**
     * Obtiene el plano interactivo de un proyecto
     */
    @Transactional(readOnly = true)
    public PlanoInteractivoResponse obtenerPlanoInteractivo(Long proyectoId) {
        Long tenantId = getTenantId();
        log.info("Obteniendo plano interactivo del proyecto {} para tenant: {}", proyectoId, tenantId);

        // Validar proyecto
        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + proyectoId));

        // Buscar plano activo (PLANO_PROYECTO o IMAGEN_PROYECTO)
        Archivo plano = archivoRepository
                .findByProyectoYTipo(tenantId, proyectoId, TipoArchivo.PLANO_PROYECTO)
                .stream()
                .filter(Archivo::getEsActivo)
                .findFirst()
                .orElse(null);

        // Si no hay PLANO_PROYECTO, buscar IMAGEN_PROYECTO
        if (plano == null) {
            plano = archivoRepository
                    .findByProyectoYTipo(tenantId, proyectoId, TipoArchivo.IMAGEN_PROYECTO)
                    .stream()
                    .filter(Archivo::getEsActivo)
                    .findFirst()
                    .orElse(null);
        }

        // Obtener todos los terrenos del proyecto
        List<Terreno> terrenos = terrenoRepository
                .findByTenantIdAndProyectoIdAndDeletedFalseOrderByNumeroLoteAsc(tenantId, proyectoId);

        // Obtener fases para incluir nombres
        Map<Long, String> fasesMap = new HashMap<>();
        List<Fase> fases = faseRepository.findByTenantIdAndProyectoIdAndDeletedFalse(tenantId, proyectoId);
        for (Fase fase : fases) {
            fasesMap.put(fase.getId(), fase.getNombre());
        }

        // Mapear terrenos a visualización
        List<TerrenoVisualizacionResponse> terrenosVisualizacion = terrenos.stream()
                .map(terreno -> mapearTerrenoAVisualizacion(terreno, fasesMap))
                .collect(Collectors.toList());

        // Calcular estadísticas
        long totalTerrenos = terrenos.size();
        long terrenosConCoordenadas = terrenos.stream()
                .filter(t -> t.getCoordenadasPlano() != null && !t.getCoordenadasPlano().isBlank())
                .count();
        long terrenosSinCoordenadas = totalTerrenos - terrenosConCoordenadas;

        long terrenosDisponibles = terrenos.stream()
                .filter(t -> t.getEstado() == EstadoTerreno.DISPONIBLE)
                .count();
        long terrenosApartados = terrenos.stream()
                .filter(t -> t.getEstado() == EstadoTerreno.APARTADO)
                .count();
        long terrenosVendidos = terrenos.stream()
                .filter(t -> t.getEstado() == EstadoTerreno.VENDIDO)
                .count();

        // Construir respuesta
        return PlanoInteractivoResponse.builder()
                .proyectoId(proyecto.getId())
                .proyectoNombre(proyecto.getNombre())
                .planoArchivoId(plano != null ? plano.getId() : null)
                .planoUrl(plano != null ? "/api/v1/archivos/" + plano.getId() + "/download" : null)
                .planoNombre(plano != null ? plano.getNombreOriginal() : null)
                .terrenos(terrenosVisualizacion)
                .totalTerrenos((int) totalTerrenos)
                .terrenosDisponibles((int) terrenosDisponibles)
                .terrenosApartados((int) terrenosApartados)
                .terrenosVendidos((int) terrenosVendidos)
                .terrenosConCoordenadas((int) terrenosConCoordenadas)
                .terrenosSinCoordenadas((int) terrenosSinCoordenadas)
                .build();
    }

    /**
     * Mapea un Terreno a TerrenoVisualizacionResponse
     */
    private TerrenoVisualizacionResponse mapearTerrenoAVisualizacion(Terreno terreno, Map<Long, String> fasesMap) {
        // Parsear coordenadas JSON a objeto
        CoordenadasPlano coordenadas = null;
        if (terreno.getCoordenadasPlano() != null && !terreno.getCoordenadasPlano().isBlank()) {
            try {
                coordenadas = objectMapper.readValue(terreno.getCoordenadasPlano(), CoordenadasPlano.class);
            } catch (JsonProcessingException e) {
                log.warn("Error al parsear coordenadas del terreno {}: {}", terreno.getId(), e.getMessage());
            }
        }

        String faseNombre = terreno.getFaseId() != null ? fasesMap.get(terreno.getFaseId()) : null;

        TerrenoVisualizacionResponse response = TerrenoVisualizacionResponse.builder()
                .id(terreno.getId())
                .numeroLote(terreno.getNumeroLote())
                .manzana(terreno.getManzana())
                .estado(terreno.getEstado())
                .coordenadas(coordenadas)
                .area(terreno.getArea())
                .precioFinal(terreno.getPrecioFinal())
                .caracteristicas(terreno.getCaracteristicas())
                .observaciones(terreno.getObservaciones())
                .faseId(terreno.getFaseId())
                .faseNombre(faseNombre)
                .build();

        // Establecer color automáticamente según estado
        response.setEstadoAndColor(terreno.getEstado());

        return response;
    }
}
