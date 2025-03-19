package org.springframework.samples.petclinic.vets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.discovery.enabled=false",
    "spring.cloud.loadbalancer.enabled=false",
    "eureka.client.enabled=false",
    "spring.zipkin.enabled=false"
})
class VetsServiceApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public Object mockDiscoveryClient() {
            return null;
        }
    }

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }

    @Test
    void mainMethodStartsApplication() {
        // This test verifies that the main method can be called without throwing exceptions
        VetsServiceApplication.main(new String[]{
            "--spring.cloud.discovery.enabled=false",
            "--spring.cloud.config.enabled=false",
            "--spring.cloud.config.discovery.enabled=false",
            "--spring.cloud.loadbalancer.enabled=false",
            "--eureka.client.enabled=false",
            "--spring.zipkin.enabled=false",
            "--server.port=0"
        });
    }
} 