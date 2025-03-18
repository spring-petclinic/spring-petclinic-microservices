package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PetTypeTest {

    private PetType petType;

    @BeforeEach
    void setUp() {
        petType = new PetType();
    }

    @Test
    void testGettersAndSetters() {
        Integer id = 1;
        String name = "cat";

        petType.setId(id);
        petType.setName(name);

        assertEquals(id, petType.getId());
        assertEquals(name, petType.getName());
    }
} 