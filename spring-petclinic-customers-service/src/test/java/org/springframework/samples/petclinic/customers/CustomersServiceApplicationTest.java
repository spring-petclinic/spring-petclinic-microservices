package org.springframework.samples.petclinic.customers;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CustomersServiceApplicationTest {

    @Test
    void mainMethodShouldRunWithoutExceptions() {
        try (MockedStatic<SpringApplication> mockedStatic = Mockito.mockStatic(SpringApplication.class)) {
            assertDoesNotThrow(() -> CustomersServiceApplication.main(new String[]{}));
            mockedStatic.verify(() -> SpringApplication.run(CustomersServiceApplication.class, new String[]{}), Mockito.times(1));
        }
    }
}
