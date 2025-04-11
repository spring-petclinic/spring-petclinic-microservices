package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {
        // Arrange
        String errorMessage = "Resource not found";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // Assert
        assertThat(exception.getMessage()).isEqualTo(errorMessage);
    }

    @Test
    void testResponseStatusAnnotation() {
        // Arrange & Act
        ResponseStatus annotation = ResourceNotFoundException.class.getAnnotation(ResponseStatus.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
