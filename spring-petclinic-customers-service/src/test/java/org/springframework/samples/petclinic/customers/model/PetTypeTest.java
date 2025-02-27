package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PetTypeTest {

    @Test
    void testPetTypeSettersAndGetters() {
        PetType type = new PetType();
        type.setId(3);
        type.setName("Cat");

        assertEquals(3, type.getId());
        assertEquals("Cat", type.getName());
    }
}
