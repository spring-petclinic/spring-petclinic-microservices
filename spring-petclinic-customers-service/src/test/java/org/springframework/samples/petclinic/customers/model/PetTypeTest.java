package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class PetTypeTest {

    private PetType petType;

    @BeforeEach
    void setUp() {
        petType = new PetType();
        petType.setId(1);
        petType.setName("dog");
    }

    @Test
    @DisplayName("Should get and set id correctly")
    void shouldGetAndSetIdCorrectly() {
        // When
        petType.setId(2);

        // Then
        assertEquals(2, petType.getId());
    }

    @Test
    @DisplayName("Should get and set name correctly")
    void shouldGetAndSetNameCorrectly() {
        // When
        petType.setName("cat");

        // Then
        assertEquals("cat", petType.getName());
    }
}
