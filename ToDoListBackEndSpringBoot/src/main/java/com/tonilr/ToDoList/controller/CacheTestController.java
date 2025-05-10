package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/cache-test")
public class CacheTestController {

    private static final Logger logger = LoggerFactory.getLogger(CacheTestController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/set")
    public ResponseEntity<?> setValue(
            @RequestParam String key, 
            @RequestParam String value) {
        try {
            logger.info("Intentando guardar en Redis - key: {}, value: {}", key, value);
            redisTemplate.opsForValue().set(key, value);
            logger.info("Valor guardado exitosamente");
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Valor guardado exitosamente",
                    "key", key,
                    "value", value
                ));
        } catch (Exception e) {
            logger.error("Error al guardar en Redis", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error al guardar en Redis",
                    "message", e.getMessage(),
                    "stackTrace", e.getStackTrace()[0].toString()
                ));
        }
    }

    @GetMapping("/get/{key}")
    public ResponseEntity<?> getValue(@PathVariable String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                .body(Map.of(
                    "key", key,
                    "value", value
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error al obtener de Redis",
                    "message", e.getMessage()
                ));
        }
    }

    @PostMapping("/set-with-ttl")
    public ResponseEntity<?> setValueWithTTL(
            @RequestParam String key, 
            @RequestParam String value, 
            @RequestParam long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Valor guardado con TTL exitosamente",
                    "key", key,
                    "value", value,
                    "ttl", ttlSeconds
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error al guardar en Redis con TTL",
                    "message", e.getMessage()
                ));
        }
    }

    @DeleteMapping("/delete/{key}")
    public ResponseEntity<?> deleteValue(@PathVariable String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Clave eliminada exitosamente",
                    "key", key,
                    "deleted", deleted
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error al eliminar de Redis",
                    "message", e.getMessage()
                ));
        }
    }
}
