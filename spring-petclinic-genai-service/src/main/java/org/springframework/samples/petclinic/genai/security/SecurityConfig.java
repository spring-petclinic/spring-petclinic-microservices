package org.springframework.samples.petclinic.genai.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
        ServerHttpSecurity http) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            .authorizeExchange(exchange -> exchange

                // Health endpoints remain public
                .pathMatchers(
                    "/actuator/health",
                    "/actuator/info"
                )
                .permitAll()

                // GenAI APIs require a valid JWT
                .pathMatchers("/**")
                .authenticated()

                // Everything else also requires authentication
                .anyExchange()
                .authenticated()
            )

            // Validate OAuth2 JWT
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            )

            .build();
    }
}
