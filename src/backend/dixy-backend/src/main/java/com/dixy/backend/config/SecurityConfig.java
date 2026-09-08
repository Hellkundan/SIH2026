package com.dixy.backend.config;

import com.dixy.backend.dto.ApiResponse;
import com.dixy.backend.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Core security configuration for the DIXY backend platform.
 * Enforces stateless JWT authentication, URL-level RBAC rules, CORS,
 * and standard JSON error translation for unauthorized and forbidden access.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://127.0.0.1:3000,http://127.0.0.1:5173}")
    private List<String> corsAllowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .contentTypeOptions(org.springframework.security.config.Customizer.withDefaults())
                .frameOptions(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig::deny)
                .xssProtection(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.XXssConfig::disable)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    ApiResponse<?> apiResponse = ApiResponse.error(
                            "Full authentication is required to access this resource",
                            HttpServletResponse.SC_UNAUTHORIZED,
                            request.getRequestURI()
                    );
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getOutputStream(), apiResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    ApiResponse<?> apiResponse = ApiResponse.error(
                            "You do not have permission to access this resource",
                            HttpServletResponse.SC_FORBIDDEN,
                            request.getRequestURI()
                    );
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getOutputStream(), apiResponse);
                })
            )
            .authorizeHttpRequests(auth -> auth
                // Public access
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Authenticated user profile
                .requestMatchers("/api/v1/auth/me").authenticated()

                // Role-based access control
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/tender/**").hasAnyRole("ADMIN", "PROCUREMENT_OFFICER")
                .requestMatchers("/api/v1/verification/**").hasAnyRole("ADMIN", "PROCUREMENT_OFFICER")
                .requestMatchers("/api/v1/compliance/**").hasAnyRole("ADMIN", "PROCUREMENT_OFFICER")
                .requestMatchers("/api/v1/audit/**").hasAnyRole("ADMIN", "AUDITOR")
                .requestMatchers("/api/v1/bidder/**").hasAnyRole("ADMIN", "BIDDER")
                .requestMatchers("/api/v1/documents/**").hasAnyRole("ADMIN", "PROCUREMENT_OFFICER", "BIDDER")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = corsAllowedOrigins != null && !corsAllowedOrigins.isEmpty()
                ? corsAllowedOrigins
                : List.of("http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173");

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Requested-With"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
