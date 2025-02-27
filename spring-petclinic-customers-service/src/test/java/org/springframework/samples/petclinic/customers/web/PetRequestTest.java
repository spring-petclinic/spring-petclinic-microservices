package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetRequestTest {

    @Test
    void testPetRequest() {
        Date birthDate = new Date();
        PetRequest request = new PetRequest(1, birthDate, "Charlie", 2);

        assertEquals(1, request.id());
        assertEquals(birthDate, request.birthDate());
        assertEquals("Charlie", request.name());
        assertEquals(2, request.typeId());
    }
}
