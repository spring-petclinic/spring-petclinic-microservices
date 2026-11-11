package org.springframework.samples.petclinic.visits.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Visits Service API")
                .description("Spring PetClinic Visits Service REST API Documentation")
                .version("v1"))
            .servers(List.of(
                new Server().url("/api/visit").description("API Gateway"),
                new Server().url("/").description("Direct Service Access")
            ));
    }
}

