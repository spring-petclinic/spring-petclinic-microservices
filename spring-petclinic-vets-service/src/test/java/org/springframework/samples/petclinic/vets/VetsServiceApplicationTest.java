// src/test/java/org/springframework/samples/petclinic/vets/VetsServiceApplicationTest.java
package org.springframework.samples.petclinic.vets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Simple test to check if the Spring application context loads successfully.
 * This primarily serves to increase coverage for the main application class.
 */
@SpringBootTest // Loads the full application context
@ActiveProfiles("test") // Use test profile to avoid external dependencies like Eureka if needed
class VetsServiceApplicationTest {

    @Test
    void contextLoads() {
        // If the application context loads without throwing an exception,
        // this test passes. It implicitly covers the execution of
        // VetsServiceApplication during context initialization.
        System.out.println("VetsServiceApplication context loaded successfully for test.");
    }

    // Note: Testing the main(String[] args) method directly is generally discouraged
    // as it starts the application and might have side effects or hang.
    // @Test
    // void applicationMainStarts() {
    //     VetsServiceApplication.main(new String[]{});
    //     // This test would likely need more setup/assertions and might not terminate cleanly.
    // }
}