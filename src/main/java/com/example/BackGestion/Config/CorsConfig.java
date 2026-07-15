package com.example.BackGestion.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Front local (Angular) + dominio propio, incluyendo cualquier subdominio
                        // (ej. app.martin-romero.cl, api.martin-romero.cl).
                        .allowedOriginPatterns(
                                "http://localhost:4200",
                                "https://martin-romero.cl",
                                "https://*.martin-romero.cl"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }
}
