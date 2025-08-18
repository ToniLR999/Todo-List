package com.tonilr.ToDoList.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig {

    @Autowired
    private CorsFilter corsFilter;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Configures the AuthenticationManager bean for JWT authentication.
     * @param authConfig Authentication configuration
     * @return Configured AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configures the security filter chain with authentication and authorization rules.
     * Sets up JWT authentication, CSRF protection, CORS, and endpoint access control.
     * @param http HttpSecurity object to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .ignoringRequestMatchers("/api/**")
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/users/register").permitAll()
            .requestMatchers("/api/app-status/**").permitAll()
            .requestMatchers("/api/system/**").permitAll()
            .requestMatchers("/api/test/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers("/api/users/profile").authenticated()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .cors(Customizer.withDefaults());
        
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(frontendUrl, "https://*.netlify.app"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
        cfg.setAllowCredentials(true);
    
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
