package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    private Vet vet;

    @BeforeEach
    void setUp() {
        vet = new Vet();
    }

    @Test
    void testConstructorAndGetters() {
        Vet v = new Vet("John");
        v.setLastName("Doe");
        assertEquals("John", v.getFirstName());
        assertEquals("Doe", v.getLastName());
    }

    @Test
    void testSettersAndGetters() {
        vet.setId(100);
        vet.setFirstName("Jane");
        vet.setLastName("Smith");

        assertEquals(100, vet.getId());
        assertEquals("Jane", vet.getFirstName());
        assertEquals("Smith", vet.getLastName());
    }

    @Test
    void testAddSpecialty() {
        Specialty spec = new Specialty();
        spec.setName("Surgery");

        vet.addSpecialty(spec);
        List<Specialty> specialties = vet.getSpecialties();

        assertEquals(1, specialties.size());
        assertEquals("Surgery", specialties.get(0).getName());
    }

    @Test
    void testGetNrOfSpecialties() {
        assertEquals(0, vet.getNrOfSpecialties());

        Specialty spec1 = new Specialty();
        Specialty spec2 = new Specialty();
        vet.addSpecialty(spec1);
        vet.addSpecialty(spec2);

        assertEquals(2, vet.getNrOfSpecialties());
    }

    @Test
    void testSpecialtiesSorting() {
        Specialty spec1 = new Specialty();
        spec1.setName("Dentistry");

        Specialty spec2 = new Specialty();
        spec2.setName("Anesthesiology");

        vet.addSpecialty(spec1);
        vet.addSpecialty(spec2);

        List<Specialty> sorted = vet.getSpecialties();

        assertEquals(2, sorted.size());
        assertEquals("Anesthesiology", sorted.get(0).getName());
        assertEquals("Dentistry", sorted.get(1).getName());
    }

    @Test
    void testGetSpecialtiesUnmodifiable() {
        Specialty spec = new Specialty();
        spec.setName("Radiology");

        vet.addSpecialty(spec);
        List<Specialty> list = vet.getSpecialties();

        assertThrows(UnsupportedOperationException.class, () -> {
            list.add(new Specialty());
        });
    }
}
