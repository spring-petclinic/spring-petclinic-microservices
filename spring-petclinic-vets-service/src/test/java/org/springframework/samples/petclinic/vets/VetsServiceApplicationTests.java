package org.springframework.samples.petclinic.vets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
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
} 