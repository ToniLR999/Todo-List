package com.tonilr.ToDoList.controller;

import com.tonilr.ToDoList.dto.LoginDTO;
import com.tonilr.ToDoList.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * Provides endpoints for login, logout, authentication check, and CSRF token retrieval.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Authenticates a user and returns a JWT token if successful.
     * @param loginDTO User credentials
     * @return JWT token and username, or error message
     */
    @Operation(summary = "User login")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),
                    loginDTO.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            Map<String, String> response = new HashMap<>();
            response.put("token", jwt);
            response.put("username", loginDTO.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Authentication error: " + e.getMessage());
        }
    }

    /**
     * Logs out the current user by clearing the security context.
     */
    @Operation(summary = "User logout")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    /**
     * Checks if the user is authenticated.
     * @return HTTP 200 if authenticated
     */
    @Operation(summary = "Check authentication")
    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the CSRF token for the current session.
     * @param request HTTP request
     * @return CSRF token (if enabled)
     */
    @GetMapping("/csrf")
    public ResponseEntity<?> getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return ResponseEntity.ok().build();
    }
}
