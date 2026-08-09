package org.springframework.samples.petclinic.admin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // Admin Server is a REST/API-style service
            .csrf(AbstractHttpConfigurer::disable)

            // Do not create HTTP sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Kubernetes/Docker/load-balancer health check
                .requestMatchers("/actuator/health").permitAll()

                // Everything else requires a valid JWT
                .anyRequest().authenticated()
            )

            // Validate Bearer JWT using issuer-uri from application.yml
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> {})
            );

        return http.build();
    }
}
