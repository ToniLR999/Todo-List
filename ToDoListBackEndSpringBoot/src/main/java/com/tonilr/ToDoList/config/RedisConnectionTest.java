package com.tonilr.ToDoList.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void testConnection() {
        try {
            redisTemplate.opsForValue().set("test", "Redis está funcionando!");
            String value = redisTemplate.opsForValue().get("test");
            System.out.println("Conexión a Redis exitosa: " + value);
        } catch (Exception e) {
            System.err.println("Error conectando a Redis: " + e.getMessage());
        }
    }
}
