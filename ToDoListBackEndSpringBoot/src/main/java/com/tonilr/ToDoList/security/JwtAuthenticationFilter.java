package com.tonilr.ToDoList.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * JWT Authentication Filter for processing JWT tokens in HTTP requests.
 * This filter intercepts all incoming requests and validates JWT tokens
 * to establish user authentication context.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor for JwtAuthenticationFilter.
     * @param tokenProvider Service for JWT token operations
     * @param userDetailsService Service for loading user details
     */
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Main filter method that processes each HTTP request.
     * Extracts JWT token from request headers, validates it, and sets up
     * Spring Security authentication context if token is valid.
     * 
     * @param request The HTTP request being processed
     * @param response The HTTP response
     * @param filterChain The filter chain to continue processing
     * @throws ServletException If a servlet error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Extract JWT token from the request
        String token = getJwtFromRequest(request);

        // If token exists, validate and set up authentication
        if (StringUtils.hasText(token)) {
            try {
                // Extract username from JWT token
                String username = tokenProvider.getUsernameFromJWT(token);
                
                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Create authentication token with user details and authorities
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Set authentication in Spring Security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Only log authentication errors in production
                log.error("JWT token validation failed: {}", e.getMessage());
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header of the HTTP request.
     * Looks for the "Bearer " prefix and returns the token part.
     * 
     * @param request The HTTP request containing the Authorization header
     * @return The JWT token string, or null if not found or invalid format
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check if Authorization header exists and has Bearer prefix
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Remove "Bearer " prefix and return the token
            return bearerToken.substring(7);
        }
        return null;
    }
}