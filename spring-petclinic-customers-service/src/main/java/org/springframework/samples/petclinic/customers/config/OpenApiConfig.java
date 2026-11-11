package org.springframework.samples.petclinic.customers.config;

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
                .title("Customers Service API")
                .description("Spring PetClinic Customers Service REST API Documentation")
                .version("v1"))
            .servers(List.of(
                new Server().url("/api/customer").description("API Gateway"),
                new Server().url("/").description("Direct Service Access")
            ));
    }
}

