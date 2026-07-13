package com.inmobiliaria.terrenos.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.terrenos.application.dto.plano.CvEngineResponse;
import com.inmobiliaria.terrenos.application.dto.plano.ConfirmarIngestaRequest;
import com.inmobiliaria.terrenos.domain.entity.Terreno;
import com.inmobiliaria.terrenos.domain.repository.TerrenoRepository;
import com.inmobiliaria.terrenos.infrastructure.cv.PlanoStatusStore;
import com.inmobiliaria.terrenos.infrastructure.tenant.TenantContext;
import com.inmobiliaria.terrenos.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
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
import java.util.stream.Collectors;

/**
 * Servicio de ingesta de planos con análisis de Computer Vision.
 *
 * Responsabilidades (SRP):
 * - Guardar el archivo de forma segura
 * - Orquestar la comunicación con el CV Engine
 * - Persistir los lotes confirmados
 * - Notificar en tiempo real al cliente (SSE)
 *
 * @author Kevin
 * @version 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlanoIngestaService {

    private final TerrenoRepository terrenoRepository;
    private final RestTemplate restTemplate;          // Inyectado como Bean (DIP)
    private final PlanoStatusStore statusStore;
    private final ObjectMapper objectMapper;

    @Value("${CV_ENGINE_URL:http://localhost:8000}")
    private String cvEngineUrl;

    private static final String BASE_UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024; // 100 MB

    /**
     * Recibe el nombre almacenado del archivo (ya guardado en disco por ArchivoService)
     * y lanza el análisis asíncrono en el pool de hilos.
     * El frontend llama a este método cuando usa el flujo de subida con ImageUploader.
     */
    public String iniciarAnalisisPorNombre(Long proyectoId, String nombreAlmacenado) {
        String tareaId = UUID.randomUUID().toString();
        // Construir ruta relativa dentro del volumen uploads
        String relativePath = "planos/proyecto_" + proyectoId + "/" + nombreAlmacenado;
        Long tenantId = TenantContext.getTenantId();
        procesarPlanoAsync(tareaId, relativePath, tenantId);
        return tareaId;
    }

    /**
     * Guarda el plano en disco y lanza el análisis de CV en un hilo separado.
     * Retorna inmediatamente un tareaId para que el cliente se suscriba a SSE.
     *
     * @return tareaId UUID para trackear el estado del análisis
     */
    public String iniciarAnalisis(Long proyectoId, MultipartFile file) throws IOException {
        validarArchivo(file);

        String tareaId = UUID.randomUUID().toString();
        Path rutaArchivo = guardarArchivoSeguro(proyectoId, file);
        String relativePath = "planos/proyecto_" + proyectoId + "/" + rutaArchivo.getFileName();

        // Lanza el análisis en el ThreadPool dedicado (no bloquea el hilo HTTP)
        Long tenantId = TenantContext.getTenantId();
        procesarPlanoAsync(tareaId, relativePath, tenantId);

        return tareaId;
    }

    /**
     * Procesamiento asíncrono en el pool de hilos "planoTaskExecutor".
     * Notifica al cliente via SSE en cada etapa del proceso.
     */
    @Async("planoTaskExecutor")
    public void procesarPlanoAsync(String tareaId, String relativePath, Long tenantId) {
        log.info("[Tarea {}] Iniciando análisis de plano: {}", tareaId, relativePath);
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
            // Error del CV Engine (imagen corrupta, formato no soportado)
            String msg = String.format("{\"mensaje\":\"CV Engine rechazó el archivo: %s\"}",
                    e.getResponseBodyAsString().replace("\"", "'"));
            log.error("[Tarea {}] Error en CV Engine: {}", tareaId, e.getResponseBodyAsString());
            statusStore.notificar(tareaId, "error", msg);

        } catch (ResourceAccessException e) {
            // CV Engine no disponible (timeout o contenedor caído)
            log.error("[Tarea {}] CV Engine no disponible: {}", tareaId, e.getMessage());
            statusStore.notificar(tareaId, "error", "{\"mensaje\":\"El servicio de análisis no está disponible. Verifique que el contenedor cv-engine esté activo.\"}");

        } catch (Exception e) {
            log.error("[Tarea {}] Error inesperado: ", tareaId, e);
            statusStore.notificar(tareaId, "error", "{\"mensaje\":\"Error interno al procesar el plano.\"}");
        }
    }

    // =========================================================================
    // Persistencia (SRP: método dedicado)
    // =========================================================================

    @Transactional
    public void confirmarIngesta(ConfirmarIngestaRequest request) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("No se encontró tenant_id en el contexto", HttpStatus.UNAUTHORIZED);
        }

        final Long finalTenantId = tenantId;

        List<Terreno> nuevosTerrenos = request.getLotes().stream()
                .map(dto -> Terreno.builder()
                        .tenantId(finalTenantId)
                        .proyectoId(request.getProyectoId())
                        .numeroLote(dto.getNumeroLote() != null ? dto.getNumeroLote() : "S/N")
                        .area(dto.getArea() != null ? dto.getArea() : BigDecimal.ZERO)
                        .precioBase(BigDecimal.ZERO)
                        .coordenadasPlano(dto.getCoordenadasPlanoJson())
                        .build())
                .collect(Collectors.toList());

        terrenoRepository.saveAll(nuevosTerrenos);
        log.info("[Tenant {}] {} lotes ingresados para proyecto {}",
                finalTenantId, nuevosTerrenos.size(), request.getProyectoId());
    }

    // =========================================================================
    // Métodos privados (SRP: extracción de responsabilidades)
    // =========================================================================

    /**
     * Guarda el archivo usando streams para no cargar el binario completo en RAM.
     * Sanitiza el nombre del archivo para prevenir Path Traversal.
     */
    private Path guardarArchivoSeguro(Long proyectoId, MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(BASE_UPLOAD_DIR, "planos", "proyecto_" + proyectoId);
        Files.createDirectories(uploadDir);

        // Sanitizar nombre: extraer solo el nombre base, sin rutas relativas (../../../)
        String nombreOriginal = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename(), "Nombre de archivo nulo"));
        String extension = nombreOriginal.contains(".")
                ? nombreOriginal.substring(nombreOriginal.lastIndexOf("."))
                : "";
        String nombreSeguro = UUID.randomUUID() + extension;  // Nombre final sin ningún input del usuario

        Path destino = uploadDir.resolve(nombreSeguro);

        // Copiar con InputStream en lugar de getBytes() → no carga el archivo completo en heap
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        log.info("Archivo guardado en: {}", destino);
        return destino;
    }

    /**
     * Valida extensión y tamaño máximo del archivo antes de procesarlo.
     */
    private void validarArchivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("El archivo está vacío", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException("El archivo supera el límite de 100MB", HttpStatus.BAD_REQUEST);
        }
        String nombre = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename(), "Nombre nulo"));
        String extension = nombre.contains(".")
                ? nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!List.of("pdf", "png", "jpg", "jpeg").contains(extension)) {
            throw new BusinessException(
                    "Formato no soportado: " + extension + ". Use PDF, PNG o JPG.", HttpStatus.BAD_REQUEST);
        }
    }
}
