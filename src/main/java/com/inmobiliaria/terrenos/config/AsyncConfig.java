package com.inmobiliaria.terrenos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración del pool de hilos para tareas asincrónicas pesadas.
 * Utilizado por @Async en el procesamiento de planos.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Executor dedicado para el análisis de planos (CPU-bound).
     * Separado del pool general para no bloquear otros @Async del sistema.
     */
    @Bean(name = "planoTaskExecutor")
    public Executor planoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);          // 3 hilos siempre activos
        executor.setMaxPoolSize(10);           // hasta 10 bajo alta carga (multi-tenant)
        executor.setQueueCapacity(50);         // cola de 50 planos pendientes
        executor.setThreadNamePrefix("plano-cv-"); // logs claros para depuración
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
