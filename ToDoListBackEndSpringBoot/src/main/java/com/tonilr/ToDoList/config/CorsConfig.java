package com.tonilr.ToDoList.config;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.web.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

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
        config.addAllowedOrigin("http://localhost:4200"); // Desarrollo local
        config.addAllowedOrigin("https://localhost:4200"); // Desarrollo local HTTPS
        config.addAllowedOrigin("https://todolist-tonilr.netlify.app"); // Netlify
        config.addAllowedOrigin("https://*.netlify.app"); // Cualquier subdominio de Netlify
        
        // Configurar métodos HTTP permitidos
        config.addAllowedMethod("*"); // Permitir todos los métodos
        
        // Configurar headers permitidos
        config.addAllowedHeader("*"); // Permitir todos los headers
        
        // Permitir credenciales (cookies, headers de autorización)
        config.setAllowCredentials(true);
        
        // Configurar tiempo máximo de cache para las respuestas preflight
        config.setMaxAge(3600L);
        
        // Configurar headers expuestos
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("Accept");
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
