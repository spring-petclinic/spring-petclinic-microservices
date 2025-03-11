package org.springframework.samples.petclinic.vets;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VetsServiceApplicationTest {

    @Test
    void mainMethodShouldRunWithoutExceptions() {
        try (MockedStatic<SpringApplication> mockedStatic = Mockito.mockStatic(SpringApplication.class)) {
            assertDoesNotThrow(() -> VetsServiceApplication.main(new String[]{}));
            mockedStatic.verify(() -> SpringApplication.run(VetsServiceApplication.class, new String[]{}), Mockito.times(1));
        }
    }
}
