package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheManagementController {

    @Autowired
    private CacheService cacheService;

    @PostMapping("/evict/user/{username}")
    public ResponseEntity<?> evictUserCache(@PathVariable String username) {
        try {
            cacheService.evictUserCache(username);
            return ResponseEntity.ok(Map.of(
                "message", "Caché del usuario evictado exitosamente",
                "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al evictar caché del usuario",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/evict/all")
    public ResponseEntity<?> evictAllCache() {
        try {
            cacheService.evictAllCache();
            return ResponseEntity.ok(Map.of(
                "message", "Todo el caché evictado exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al evictar todo el caché",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/stats/{username}")
    public ResponseEntity<?> getUserStats(@PathVariable String username) {
        try {
            String stats = cacheService.getUserStats(username);
            return ResponseEntity.ok(Map.of(
                "username", username,
                "stats", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al obtener estadísticas del usuario",
                "message", e.getMessage()
            ));
        }
    }
}
