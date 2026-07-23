package com.bookmarks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Real request-time authentication/authorization for protected routes is handled by
        // AuthInterceptor (validates the Bearer token via JwtUtil and resolves the current user)
        // and BookmarkController (enforces per-user ownership) - both run at the MVC layer, after
        // Spring Security's own filter chain. `anyRequest().authenticated()` here would reject
        // every request with "Access Denied" before AuthInterceptor ever runs, since Spring
        // Security has no registered filter that understands our JWT scheme. This filter chain
        // only needs to disable CSRF/sessions for a stateless REST API, not duplicate auth.
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST API
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
