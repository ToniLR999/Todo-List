package com.tonilr.ToDoList.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class TaskLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    // Lista temporal para almacenar tareas (en un caso real usaríamos una base de datos)
    private static final List<Task> tasks = new ArrayList<>();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        
        // Configurar headers CORS
        Map<String, String> headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        headers.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeaders(headers);
        
        try {
            // Si es una petición OPTIONS, retornar inmediatamente
            if ("OPTIONS".equals(input.getHttpMethod())) {
                response.setStatusCode(200);
                return response;
            }
            
            // Procesar la petición según el método HTTP
            switch (input.getHttpMethod()) {
                case "GET":
                    return handleGetTasks(input, context);
                case "POST":
                    return handleCreateTask(input, context);
                default:
                    response.setStatusCode(404);
                    response.setBody("{\"message\": \"Método no soportado\"}");
            }
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error interno del servidor: " + e.getMessage() + "\"}");
        }
        
        return response;
    }

    private APIGatewayProxyResponseEvent handleGetTasks(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(tasks));
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error al obtener tareas\"}");
        }
        return response;
    }

    private APIGatewayProxyResponseEvent handleCreateTask(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            Task newTask = objectMapper.readValue(input.getBody(), Task.class);
            tasks.add(newTask);
            response.setStatusCode(201);
            response.setBody(objectMapper.writeValueAsString(newTask));
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error al crear tarea\"}");
        }
        return response;
    }
}

// Clase para representar una tarea
class Task {
    private String id;
    private String title;
    private String description;
    private boolean completed;

    // Constructor vacío necesario para Jackson
    public Task() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
} 