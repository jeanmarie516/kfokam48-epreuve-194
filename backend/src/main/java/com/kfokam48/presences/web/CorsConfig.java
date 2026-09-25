package com.kfokam48.presences.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Le frontend Next.js (dev) est servi sur un port pouvant être surchargé via .env
 *  (K48_FRONT_PORT). On accepte toute origine pour /api/** en local : l'application
 *  n'utilise aucune authentification (Q1) ni cookie, l'ouverture large du CORS ne
 *  présente pas de risque et rend l'app utilisable sur n'importe quel port local. */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
            }
        };
    }
}
