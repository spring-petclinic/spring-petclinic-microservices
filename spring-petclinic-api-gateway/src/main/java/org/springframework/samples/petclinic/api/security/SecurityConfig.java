package org.springframework.samples.petclinic.api.security;

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

                /*
                 * The PetClinic UI and static resources
                 * are publicly accessible.
                 */
                .pathMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/webjars/**",
                    "/images/**",
                    "/static/**",
                    "/favicon.ico"
                )
                .permitAll()

                /*
                 * Health/actuator endpoints are kept
                 * accessible for infrastructure checks.
                 */
                .pathMatchers(
                    "/actuator/health",
                    "/actuator/info"
                )
                .permitAll()

                /*
                 * All API Gateway routes require
                 * a valid OAuth2 access token.
                 */
                .pathMatchers("/api/**")
                .authenticated()

                /*
                 * Anything else requires authentication.
                 */
                .anyExchange()
                .authenticated()
            )

            /*
             * Configure this Gateway as an OAuth2
             * Resource Server that validates JWTs.
             */
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            )

            .build();
    }
}
