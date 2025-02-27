package org.springframework.samples.petclinic.visits;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class VisitsServiceApplicationTest {

    @Test
    void contextLoads() {
        //
    }

    @Test
    void mainMethodShouldRunWithoutExceptions() {
        try (MockedStatic<SpringApplication> mockedStatic = Mockito.mockStatic(SpringApplication.class)) {
            assertDoesNotThrow(() -> VisitsServiceApplication.main(new String[]{}));
            mockedStatic.verify(() -> SpringApplication.run(VisitsServiceApplication.class, new String[]{}), Mockito.times(1));
        }
    }
}
