package com.inmobiliaria.terrenos.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuración del cliente HTTP RestTemplate.
 * Aplica timeouts para evitar conexiones zombies hacia el CV Engine.
 * Principio DIP: RestTemplate inyectado como Bean, no instanciado en el servicio.
 *
 * @author Kevin
 * @version 1.0.0
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))  // Timeout de conexión al CV Engine
                .readTimeout(Duration.ofMinutes(3))       // Timeout de lectura (OCR puede tardar)
                .build();
    }
}
