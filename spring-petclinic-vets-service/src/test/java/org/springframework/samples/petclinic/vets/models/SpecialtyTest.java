package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpecialtyTest {

    @Test
    void testSpecialtyGettersAndSetters() {
        Specialty specialty = new Specialty();
        specialty.setName("Dentistry");
        assertEquals("Dentistry", specialty.getName());
    }

    @Test
    void testSpecialtyDefaultConstructor() {
        Specialty specialty = new Specialty();
        assertNull(specialty.getId());
        assertNull(specialty.getName());
    }
}
