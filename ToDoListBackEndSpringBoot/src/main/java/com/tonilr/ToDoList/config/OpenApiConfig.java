package com.tonilr.ToDoList.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ToDo List API")
                        .description("API completa para gestión de tareas con autenticación JWT, roles y auditoría")
                        .version("1.0"));
    }
}