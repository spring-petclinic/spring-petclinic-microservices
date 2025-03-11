package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundException() {
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException("Pet not found");
        });

        assertEquals("Pet not found", exception.getMessage());
    }
}
