package com.tonilr.ToDoList.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class TaskLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        
        try {
            // Aquí implementaremos la lógica según el método HTTP
            String httpMethod = input.getHttpMethod();
            String path = input.getPath();
            
            switch (httpMethod) {
                case "GET":
                    if (path.equals("/api/tasks")) {
                        // Lógica para obtener tareas
                        return handleGetTasks(input, context);
                    }
                    break;
                case "POST":
                    if (path.equals("/api/tasks")) {
                        // Lógica para crear tarea
                        return handleCreateTask(input, context);
                    }
                    break;
                // Agregar más casos según necesites
            }
            
            response.setStatusCode(404);
            response.setBody("{\"message\": \"Ruta no encontrada\"}");
            
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error interno del servidor\"}");
        }
        
        return response;
    }

    private APIGatewayProxyResponseEvent handleGetTasks(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            // Implementar lógica para obtener tareas
            response.setStatusCode(200);
            response.setBody("{\"message\": \"Lista de tareas\"}");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error al obtener tareas\"}");
        }
        return response;
    }

    private APIGatewayProxyResponseEvent handleCreateTask(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            // Implementar lógica para crear tarea
            response.setStatusCode(201);
            response.setBody("{\"message\": \"Tarea creada\"}");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error al crear tarea\"}");
        }
        return response;
    }
} 