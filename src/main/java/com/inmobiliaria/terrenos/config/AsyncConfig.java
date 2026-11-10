package com.inmobiliaria.terrenos.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuración del pool de hilos para tareas asincrónicas pesadas.
 * Optimizado para entorno multi-tenant de ~30 empresas concurrentes:
 * - Limita el consumo de CPU y memoria RAM ante procesamiento de planos e ingesta OCR.
 * - Utiliza política CallerRunsPolicy para proteger la JVM ante saturación de cola.
 *
 * @author Kevin
 * @version 2.0.0
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Executor dedicado para el análisis de planos (CPU y Memory bound).
     * Separado del pool general para no bloquear otros hilos del sistema ni agotar la memoria.
     */
    @Bean(name = "planoTaskExecutor")
    public Executor planoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);               // 2 hilos base dedicados
        executor.setMaxPoolSize(3);                // máximo 3 planos procesándose en paralelo para proteger RAM
        executor.setQueueCapacity(25);             // cola acotada de 25 tareas pendientes
        executor.setThreadNamePrefix("plano-cv-"); // prefijo descriptivo para métricas y logs
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // degradación elegante ante pico
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        log.info("Pool 'planoTaskExecutor' configurado: core=2, max=3, queue=25 con CallerRunsPolicy");
        return executor;
    }
}
