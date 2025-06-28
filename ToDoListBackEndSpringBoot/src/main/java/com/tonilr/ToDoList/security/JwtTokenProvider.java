package com.tonilr.ToDoList.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT token provider for authentication and authorization.
 * Handles JWT token generation, validation, and parsing for both
 * regular authentication and password reset functionality.
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    /**
     * Creates a signing key from the JWT secret for token signing and verification.
     * @return SecretKey for JWT operations
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for authenticated users.
     * @param authentication Spring Security authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts username from a JWT token.
     * @param token JWT token to parse
     * @return Username from the token subject
     */
    public String getUsernameFromJWT(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Validates a JWT token for authenticity and expiration.
     * @param authToken JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Generates a password reset token with 1-hour expiration.
     * @param username Username for the reset token
     * @return JWT token for password reset
     */
    public String generatePasswordResetToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hora
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Extracts username from a password reset token.
     * @param token Password reset JWT token
     * @return Username from the token subject
     */
    public String getUsernameFromPasswordResetToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }
}
