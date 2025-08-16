package com.tonilr.ToDoList.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for testing Redis cache operations.
 * Provides endpoints to set, get, delete, and set values with TTL in Redis.
 * Intended for development and debugging purposes.
 */
@RestController
@RequestMapping("/api/cache-test")
public class CacheTestController {

    private static final Logger logger = LoggerFactory.getLogger(CacheTestController.class);

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Sets a value in Redis for a given key.
     */
    @PostMapping("/set")
    public ResponseEntity<?> setValue(
            @RequestParam String key, 
            @RequestParam String value) {
        if (redisTemplate == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Redis not available"));
        }
        
        try {
            logger.info("Attempting to save in Redis - key: {}, value: {}", key, value);
            redisTemplate.opsForValue().set(key, value);
            logger.info("Value saved successfully");
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Value saved successfully",
                    "key", key,
                    "value", value
                ));
        } catch (Exception e) {
            logger.error("Error saving in Redis", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error saving in Redis",
                    "message", e.getMessage(),
                    "stackTrace", e.getStackTrace()[0].toString()
                ));
        }
    }

    /**
     * Retrieves a value from Redis by key.
     */
    @GetMapping("/get/{key}")
    public ResponseEntity<?> getValue(@PathVariable String key) {
        if (redisTemplate == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Redis not available"));
        }
        
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
                    "error", "Error retrieving from Redis",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * Sets a value in Redis with a time-to-live (TTL).
     */
    @PostMapping("/set-with-ttl")
    public ResponseEntity<?> setValueWithTTL(
            @RequestParam String key, 
            @RequestParam String value, 
            @RequestParam long ttlSeconds) {
        if (redisTemplate == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Redis not available"));
        }
        
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Value saved with TTL successfully",
                    "key", key,
                    "value", value,
                    "ttl", ttlSeconds
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error saving in Redis with TTL",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * Deletes a value from Redis by key.
     */
    @DeleteMapping("/delete/{key}")
    public ResponseEntity<?> deleteValue(@PathVariable String key) {
        if (redisTemplate == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Redis not available"));
        }
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Key deleted successfully",
                    "key", key,
                    "deleted", deleted
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error deleting from Redis",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * Sets a value in Redis via GET request (for testing).
     */
    @GetMapping("/set-get")
    public ResponseEntity<?> setValueGet(
            @RequestParam String key, 
            @RequestParam String value) {
        if (redisTemplate == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Redis not available"));
        }
        
        try {
            logger.info("Attempting to save in Redis (GET) - key: {}, value: {}", key, value);
            redisTemplate.opsForValue().set(key, value);
            logger.info("Value saved successfully via GET");
            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Value saved successfully via GET",
                    "key", key,
                    "value", value
                ));
        } catch (Exception e) {
            logger.error("Error saving in Redis via GET", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "error", "Error saving in Redis",
                    "message", e.getMessage()
                ));
        }
    }
}
