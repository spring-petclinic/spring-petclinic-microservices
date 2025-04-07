package org.springframework.samples.petclinic.customers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomersServiceApplicationTest {
    @Test
    void shouldBootstrapContext() {
        // Arrange
        CustomersServiceApplication application = new CustomersServiceApplication();

        // Act
        CustomersServiceApplication.main(new String[]{});

        // Assert
        assertThat(application).isNotNull();
    }
}