package com.example.demo.config;

import com.example.demo.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * KEY DESIGN DECISIONS:
 *
 * 1. STATELESS sessions — we never create an HttpSession. Every request
 *    is authenticated independently via the JWT in the Authorization header.
 *
 * 2. JwtAuthFilter runs BEFORE UsernamePasswordAuthenticationFilter so we
 *    process Bearer tokens before Spring's default form-login filter.
 *
 * 3. Only /api/v1/auth/** (register + login) are public. Everything else
 *    requires a valid JWT.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(org.springframework.security.config.Customizer.withDefaults())
            // Disable CSRF — not needed for stateless JWT APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public: health check (no JWT needed — used by UptimeRobot)
                .requestMatchers("/healthz").permitAll()
                // Public: auth endpoints only
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Public: static frontend assets served by Spring Boot
                .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/error").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // No server-side sessions — JWT carries all state
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Wire up our custom JWT filter and DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Cache-Control"));
        configuration.setExposedHeaders(java.util.List.of("Authorization"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}