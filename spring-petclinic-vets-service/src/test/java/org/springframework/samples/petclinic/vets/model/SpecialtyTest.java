package org.springframework.samples.petclinic.vets.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vets.model.Specialty;

public class SpecialtyTest {
     @Test
    void testSetAndGetName() {
        Specialty specialty = new Specialty();
        specialty.setName("Dentistry");

        assertEquals("Dentistry", specialty.getName());
    }

    @Test
    void testDefaultIdIsNull() {
        Specialty specialty = new Specialty();
        assertNull(specialty.getId(), "ID should be null before persisting");
    }

    @Test
    void testSetNameWithNull() {
        Specialty specialty = new Specialty();
        specialty.setName(null);

        assertNull(specialty.getName());
    }
}
