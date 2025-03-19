package org.springframework.samples.petclinic.customers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.config.enabled=false",
    "spring.cloud.config.discovery.enabled=false",
    "spring.cloud.loadbalancer.enabled=false",
    "eureka.client.enabled=false",
    "spring.zipkin.enabled=false"
})
class CustomersServiceApplicationTest {
    // test
    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
    }

    @Test
    void mainMethodStartsApplication() {
        // This test verifies that the main method can be called without throwing exceptions
        CustomersServiceApplication.main(new String[]{
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
