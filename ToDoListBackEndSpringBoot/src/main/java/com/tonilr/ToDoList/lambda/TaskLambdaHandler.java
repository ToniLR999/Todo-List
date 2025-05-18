package com.tonilr.ToDoList.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@Component
public class TaskLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<Task> tasks = new ArrayList<>();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        
        try {
            String httpMethod = input.getHttpMethod();
            String path = input.getPath();
            
            switch (httpMethod) {
                case "GET":
                    if (path.equals("/api/tasks")) {
                        return handleGetTasks(input, context);
                    }
                    break;
                case "POST":
                    if (path.equals("/api/tasks")) {
                        return handleCreateTask(input, context);
                    }
                    break;
                case "PUT":
                    if (path.matches("/api/tasks/\\d+")) {
                        return handleUpdateTask(input, context);
                    }
                    break;
                case "DELETE":
                    if (path.matches("/api/tasks/\\d+")) {
                        return handleDeleteTask(input, context);
                    }
                    break;
            }
            
            response.setStatusCode(404);
            response.setBody("{\"message\": \"Ruta no encontrada\"}");
            
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error interno del servidor: " + e.getMessage() + "\"}");
        }
        
        return response;
    }

    private APIGatewayProxyResponseEvent handleGetTasks(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            String tasksJson = objectMapper.writeValueAsString(tasks);
            response.setStatusCode(200);
            response.setBody(tasksJson);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"message\": \"Error al obtener las tareas\"}");
        }
        return response;
    }

    private APIGatewayProxyResponseEvent handleCreateTask(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            Task newTask = objectMapper.readValue(input.getBody(), Task.class);
            newTask.setId(tasks.size() + 1);
            tasks.add(newTask);
            
            response.setStatusCode(201);
            response.setBody(objectMapper.writeValueAsString(newTask));
        } catch (Exception e) {
            response.setStatusCode(400);
            response.setBody("{\"message\": \"Error al crear la tarea\"}");
        }
        return response;
    }

    private APIGatewayProxyResponseEvent handleUpdateTask(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            String[] pathParts = input.getPath().split("/");
            int taskId = Integer.parseInt(pathParts[pathParts.length - 1]);
            
            Task updatedTask = objectMapper.readValue(input.getBody(), Task.class);
            updatedTask.setId(taskId);
            
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId() == taskId) {
                    tasks.set(i, updatedTask);
                    response.setStatusCode(200);
                    response.setBody(objectMapper.writeValueAsString(updatedTask));
                    return response;
                }
            }
            
            response.setStatusCode(404);
            response.setBody("{\"message\": \"Tarea no encontrada\"}");
        } catch (Exception e) {
            response.setStatusCode(400);
            response.setBody("{\"message\": \"Error al actualizar la tarea\"}");
        }
        return response;
    }

    private APIGatewayProxyResponseEvent handleDeleteTask(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            String[] pathParts = input.getPath().split("/");
            int taskId = Integer.parseInt(pathParts[pathParts.length - 1]);
            
            for (int i = 0; i < tasks.size(); i++) {
                if (tasks.get(i).getId() == taskId) {
                    tasks.remove(i);
                    response.setStatusCode(204);
                    return response;
                }
            }
            
            response.setStatusCode(404);
            response.setBody("{\"message\": \"Tarea no encontrada\"}");
        } catch (Exception e) {
            response.setStatusCode(400);
            response.setBody("{\"message\": \"Error al eliminar la tarea\"}");
        }
        return response;
    }

    public static class Task {
        private int id;
        private String title;
        private String description;
        private boolean completed;

        public Task() {}

        public Task(int id, String title, String description, boolean completed) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.completed = completed;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
} 