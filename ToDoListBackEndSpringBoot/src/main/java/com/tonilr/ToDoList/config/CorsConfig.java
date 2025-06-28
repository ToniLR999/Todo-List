package com.tonilr.ToDoList.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS (Cross-Origin Resource Sharing) configuration class.
 * Configures cross-origin requests to allow the Angular frontend
 * to communicate with the Spring Boot backend API.
 */
@Configuration
public class CorsConfig {

    /**
     * Configures CORS filter to allow cross-origin requests from the Angular frontend.
     * Sets up allowed origins, methods, headers, and credentials for secure communication.
     * @return Configured CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Configurar orígenes permitidos
        config.addAllowedOrigin("http://localhost:4200"); // Angular frontend
        
        // Configurar métodos HTTP permitidos
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        
        // Configurar headers permitidos
        config.addAllowedHeader("*");
        
        // Permitir credenciales (cookies, headers de autorización)
        config.setAllowCredentials(true);
        
        // Configurar tiempo máximo de cache para las respuestas preflight
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
