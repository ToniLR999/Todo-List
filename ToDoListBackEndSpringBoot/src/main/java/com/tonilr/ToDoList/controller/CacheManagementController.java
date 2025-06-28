package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing application cache.
 * Provides endpoints to evict user-specific or all cache, and to retrieve user cache statistics.
 */
@RestController
@RequestMapping("/api/cache")
public class CacheManagementController {

    @Autowired
    private CacheService cacheService;

    /**
     * Evicts the cache for a specific user.
     * @param username The username whose cache should be evicted
     * @return Success or error message
     */
    @PostMapping("/evict/user/{username}")
    public ResponseEntity<?> evictUserCache(@PathVariable String username) {
        try {
            cacheService.evictUserCache(username);
            return ResponseEntity.ok(Map.of(
                "message", "User cache evicted successfully",
                "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error evicting user cache",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Evicts all cache entries in the application.
     * @return Success or error message
     */
    @PostMapping("/evict/all")
    public ResponseEntity<?> evictAllCache() {
        try {
            cacheService.evictAllCache();
            return ResponseEntity.ok(Map.of(
                "message", "All cache evicted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error evicting all cache",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Retrieves cache statistics for a specific user.
     * @param username The username to get stats for
     * @return User cache statistics or error message
     */
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
                "error", "Error retrieving user statistics",
                "message", e.getMessage()
            ));
        }
    }
}
