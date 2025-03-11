package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class VetTest {

    @Test
    void testVetSetterGetter() {
        Vet vet = new Vet();
        vet.setFirstName("John");
        vet.setLastName("Doe");

        assertEquals("John", vet.getFirstName(), "First name should be correctly set");
        assertEquals("Doe", vet.getLastName(), "Last name should be correctly set");
    }

    @Test
    void testAddSpecialty() {
        Vet vet = new Vet();
        Specialty specialty = new Specialty();
        specialty.setName("Surgery");

        vet.addSpecialty(specialty);

        List<Specialty> specialties = vet.getSpecialties();
        assertEquals(1, specialties.size(), "Vet should have one specialty");
        assertEquals("Surgery", specialties.get(0).getName(), "Specialty name should be correctly set");
    }

    @Test
    void testNrOfSpecialties() {
        Vet vet = new Vet();
        assertEquals(0, vet.getNrOfSpecialties(), "Initially, vet should have no specialties");

        Specialty specialty1 = new Specialty();
        specialty1.setName("Dentistry");
        vet.addSpecialty(specialty1);

        Specialty specialty2 = new Specialty();
        specialty2.setName("Surgery");
        vet.addSpecialty(specialty2);

        assertEquals(2, vet.getNrOfSpecialties(), "Vet should have two specialties");
    }
}
