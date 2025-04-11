package org.springframework.samples.petclinic.vets.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VetTest {
    private Vet vet;

    @BeforeEach
    void setUp() {
        vet = new Vet();
    }

    @Test
    void testSetAndGetFirstName() {
        vet.setFirstName("John");
        assertEquals("John", vet.getFirstName());
    }

    @Test
    void testSetAndGetLastName() {
        vet.setLastName("Doe");
        assertEquals("Doe", vet.getLastName());
    }

    @Test
    void testSetAndGetId() {
        vet.setId(101);
        assertEquals(101, vet.getId());
    }

    @Test
    void testAddSpecialtyAndGetSpecialties() {
        Specialty specialty1 = new Specialty();
        specialty1.setName("Surgery");

        Specialty specialty2 = new Specialty();
        specialty2.setName("Dentistry");

        vet.addSpecialty(specialty1);
        vet.addSpecialty(specialty2);

        List<Specialty> specialties = vet.getSpecialties();

        assertEquals(2, specialties.size());
        assertTrue(specialties.stream().anyMatch(s -> "Surgery".equals(s.getName())));
        assertTrue(specialties.stream().anyMatch(s -> "Dentistry".equals(s.getName())));
    }

    @Test
    void testNrOfSpecialties() {
        assertEquals(0, vet.getNrOfSpecialties());

        Specialty specialty = new Specialty();
        specialty.setName("Radiology");

        vet.addSpecialty(specialty);

        assertEquals(1, vet.getNrOfSpecialties());
    }

    @Test
    void testGetSpecialtiesReturnsUnmodifiableList() {
        Specialty specialty = new Specialty();
        specialty.setName("Radiology");

        vet.addSpecialty(specialty);

        List<Specialty> specialties = vet.getSpecialties();

        assertThrows(UnsupportedOperationException.class, () -> specialties.add(new Specialty()));
    }
}
