package main.java.com.tonilr.ToDoList.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

public class TaskLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            // Aquí implementarías la lógica para obtener tareas
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
            // Aquí implementarías la lógica para crear tareas
            response.setStatusCode(201);
            response.setBody("{\"message\": \"Tarea creada\"}");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error al crear tarea\"}");
        }
        return response;
    }
}
