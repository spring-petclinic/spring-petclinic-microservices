package org.springframework.samples.petclinic.visits.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http) throws Exception {

        return http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(authorize -> authorize

                /*
                 * Health endpoints remain publicly accessible
                 * for infrastructure and monitoring checks.
                 */
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info"
                )
                .permitAll()

                /*
                 * All other endpoints require
                 * a valid OAuth2 JWT.
                 */
                .anyRequest()
                .authenticated()
            )

            /*
             * Configure this service as an OAuth2
             * Resource Server that validates JWTs.
             */
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            )

            .build();
    }
}
