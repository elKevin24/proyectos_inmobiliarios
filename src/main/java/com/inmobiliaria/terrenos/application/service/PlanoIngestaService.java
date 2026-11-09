package com.inmobiliaria.terrenos.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.plano.ConfirmarIngestaRequest;
import com.inmobiliaria.terrenos.application.dto.plano.CvEngineResponse;
import com.inmobiliaria.terrenos.domain.entity.Proyecto;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.enums.EstadoTerreno;
import com.inmobiliaria.terrenos.domain.repository.ProyectoRepository;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import com.inmobiliaria.terrenos.shared.exception.PlanoException;
import com.inmobiliaria.terrenos.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Servicio de ingesta de planos con análisis de Computer Vision.
 *
 * @author Kevin
 * @version 2.1.0
 */
@Service
@Slf4j
public class PlanoIngestaService {

    private final TerrenoRepository terrenoRepository;
    private final ProyectoRepository proyectoRepository;
    private final RestTemplate restTemplate;
    private final PlanoStatusStore statusStore;
    private final ObjectMapper objectMapper;
    private final Executor planoTaskExecutor;

    @Value("${CV_ENGINE_URL:http://localhost:8000}")
    private String cvEngineUrl;

    private static final String BASE_UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024; // 100 MB

    public PlanoIngestaService(
            TerrenoRepository terrenoRepository,
            ProyectoRepository proyectoRepository,
            RestTemplate restTemplate,
            PlanoStatusStore statusStore,
            ObjectMapper objectMapper,
            @Qualifier("planoTaskExecutor") Executor planoTaskExecutor) {
        this.terrenoRepository = terrenoRepository;
        this.proyectoRepository = proyectoRepository;
        this.restTemplate = restTemplate;
        this.statusStore = statusStore;
        this.objectMapper = objectMapper;
        this.planoTaskExecutor = planoTaskExecutor;
    }

    private Long getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }
        return tenantId;
    }

    /**
     * Valida que el proyecto pertenezca al tenant actual y lanza el análisis asíncrono en planoTaskExecutor.
     */
    @Transactional(readOnly = true)
    public String iniciarAnalisisPorNombre(Long proyectoId, String nombreAlmacenado) {
        Long tenantId = getTenantId();
        proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + proyectoId));

        String tareaId = UUID.randomUUID().toString();
        String relativePath = "planos/proyecto_" + proyectoId + "/" + nombreAlmacenado;

        CompletableFuture.runAsync(
                () -> procesarPlanoAsync(tareaId, relativePath, tenantId),
                planoTaskExecutor
        );
        return tareaId;
    }

    /**
     * Guarda el plano en disco y lanza el análisis de CV en el pool dedicado.
     */
    @Transactional(readOnly = true)
    public String iniciarAnalisis(Long proyectoId, MultipartFile file) throws IOException {
        Long tenantId = getTenantId();
        proyectoRepository.findByIdAndTenantIdAndDeletedFalse(proyectoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + proyectoId));

        validarArchivo(file);

        String tareaId = UUID.randomUUID().toString();
        Path rutaArchivo = guardarArchivoSeguro(proyectoId, file);
        String relativePath = "planos/proyecto_" + proyectoId + "/" + rutaArchivo.getFileName();

        CompletableFuture.runAsync(
                () -> procesarPlanoAsync(tareaId, relativePath, tenantId),
                planoTaskExecutor
        );
        return tareaId;
    }

    /**
     * Procesamiento en el pool de hilos "planoTaskExecutor".
     * Propaga TenantContext al hilo trabajador y limpia al finalizar.
     */
    public void procesarPlanoAsync(String tareaId, String relativePath, Long tenantId) {
        TenantContext.setTenantId(tenantId);
        log.info("[Tarea {}] [Tenant {}] Iniciando análisis de plano: {}", tareaId, tenantId, relativePath);
        statusStore.notificar(tareaId, "progreso", "{\"paso\":\"Analizando imagen con IA...\",\"porcentaje\":10}");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file_path", relativePath);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            statusStore.notificar(tareaId, "progreso", "{\"paso\":\"Detectando contornos de lotes...\",\"porcentaje\":40}");

            ResponseEntity<CvEngineResponse> response = restTemplate.postForEntity(
                    cvEngineUrl + "/api/cv/extract-lots", request, CvEngineResponse.class);

            statusStore.notificar(tareaId, "progreso", "{\"paso\":\"Extrayendo texto con OCR...\",\"porcentaje\":80}");

            CvEngineResponse resultado = response.getBody();
            if (resultado == null || resultado.getLotes() == null) {
                statusStore.notificar(tareaId, "error", "{\"mensaje\":\"El motor CV no devolvió resultados\"}");
                return;
            }

            String resultadoJson = objectMapper.writeValueAsString(resultado);
            statusStore.notificar(tareaId, "completado", resultadoJson);

            log.info("[Tarea {}] Análisis completado. Lotes detectados: {}", tareaId, resultado.getTotal_lotes_detectados());

        } catch (HttpClientErrorException e) {
            String msg = String.format("{\"mensaje\":\"CV Engine rechazó el archivo: %s\"}",
                    e.getResponseBodyAsString().replace("\"", "'"));
            log.error("[Tarea {}] Error en CV Engine: {}", tareaId, e.getResponseBodyAsString());
            statusStore.notificar(tareaId, "error", msg);

        } catch (ResourceAccessException e) {
            log.error("[Tarea {}] CV Engine no disponible: {}", tareaId, e.getMessage());
            statusStore.notificar(tareaId, "error", "{\"mensaje\":\"El servicio de análisis no está disponible. Verifique que el contenedor cv-engine esté activo.\"}");

        } catch (Exception e) {
            log.error("[Tarea {}] Error inesperado: ", tareaId, e);
            statusStore.notificar(tareaId, "error", "{\"mensaje\":\"Error interno al procesar el plano.\"}");
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Valida que el proyecto pertenezca al tenant, persiste los lotes confirmados y actualiza contadores del proyecto.
     */
    @Transactional
    public void confirmarIngesta(ConfirmarIngestaRequest request) {
        Long tenantId = getTenantId();

        Proyecto proyecto = proyectoRepository.findByIdAndTenantIdAndDeletedFalse(request.getProyectoId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado con id: " + request.getProyectoId()));

        if (request.getLotes() == null || request.getLotes().isEmpty()) {
            throw new PlanoException("Debe incluir al menos un lote para confirmar la ingesta");
        }

        BigDecimal precioBaseProyecto = proyecto.getPrecioBase() != null ? proyecto.getPrecioBase() : BigDecimal.ZERO;

        List<Terreno> nuevosTerrenos = request.getLotes().stream()
                .map(dto -> Terreno.builder()
                        .tenantId(tenantId)
                        .proyectoId(proyecto.getId())
                        .numeroLote(dto.getNumeroLote() != null && !dto.getNumeroLote().isBlank() ? dto.getNumeroLote() : "S/N")
                        .area(dto.getArea() != null ? dto.getArea() : BigDecimal.ONE)
                        .precioBase(precioBaseProyecto)
                        .precioFinal(precioBaseProyecto)
                        .estado(EstadoTerreno.DISPONIBLE)
                        .coordenadasPlano(dto.getCoordenadasPlanoJson())
                        .build())
                .collect(Collectors.toList());

        terrenoRepository.saveAll(nuevosTerrenos);

        long totalTerrenos = terrenoRepository.countByTenantIdAndProyectoIdAndDeletedFalse(tenantId, proyecto.getId());
        long disponibles = terrenoRepository.countByTenantIdAndProyectoIdAndEstadoAndDeletedFalse(
                tenantId, proyecto.getId(), EstadoTerreno.DISPONIBLE);
        proyecto.setTotalTerrenos((int) totalTerrenos);
        proyecto.setTerrenosDisponibles((int) disponibles);
        proyectoRepository.save(proyecto);

        log.info("[Tenant {}] {} lotes ingresados para proyecto {}",
                tenantId, nuevosTerrenos.size(), proyecto.getId());
    }

    private Path guardarArchivoSeguro(Long proyectoId, MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(BASE_UPLOAD_DIR, "planos", "proyecto_" + proyectoId);
        Files.createDirectories(uploadDir);

        String nombreOriginal = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename(), "Nombre de archivo nulo"));
        String extension = nombreOriginal.contains(".")
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                : "";
        String nombreSeguro = UUID.randomUUID() + extension;

        Path destino = uploadDir.resolve(nombreSeguro);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        log.info("Archivo guardado en: {}", destino);
        return destino;
    }

    private void validarArchivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw PlanoException.archivoVacio();
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw PlanoException.archivoExcedeLimite(100);
        }
        String nombre = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename(), "Nombre nulo"));
        String extension = nombre.contains(".")
                ? nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!List.of("pdf", "png", "jpg", "jpeg").contains(extension)) {
            throw new PlanoException(
                    "Formato no soportado: " + extension + ". Use PDF, PNG o JPG.");
        }
    }
}
