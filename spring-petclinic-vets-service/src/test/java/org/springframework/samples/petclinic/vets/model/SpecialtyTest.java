package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecialtyTest {

    @Test
    void testSpecialtySetterGetter() {
        Specialty specialty = new Specialty();
        specialty.setName("Dentistry");

        assertNull(specialty.getId(), "ID should be null before persisting");
        assertEquals("Dentistry", specialty.getName(), "Specialty name should be correctly set");
    }
    //
}
