package com.inmobiliaria.terrenos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Aplicación principal del sistema SaaS Multi-tenant de Gestión de Venta de Terrenos.
 *
 * @author Kevin
 * @version 1.0.0
 * @since 2025-01-13
 */
@SpringBootApplication
@EnableJpaAuditing
public class TerrenosSaasApplication {

    public static void main(String[] args) {
        SpringApplication.run(TerrenosSaasApplication.class, args);
    }
}
