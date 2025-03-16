package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    @Test
    void testVetGettersAndSetters() {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        assertEquals(1, vet.getId());
        assertEquals("John", vet.getFirstName());
        assertEquals("Doe", vet.getLastName());
    }

    @Test
    void testSpecialtiesManagement() {
        Vet vet = new Vet();
        Specialty specialty1 = new Specialty();
        specialty1.setName("Surgery");
        Specialty specialty2 = new Specialty();
        specialty2.setName("Dentistry");

        vet.addSpecialty(specialty1);
        vet.addSpecialty(specialty2);

        assertEquals(2, vet.getNrOfSpecialties());
        assertTrue(vet.getSpecialtiesInternal().contains(specialty1));
        assertTrue(vet.getSpecialtiesInternal().contains(specialty2));
    }

    @Test
    void testGetSpecialtiesWithSorting() {
        Vet vet = new Vet();
        Specialty specialty1 = new Specialty();
        specialty1.setName("Dentistry");
        Specialty specialty2 = new Specialty();
        specialty2.setName("Surgery");

        vet.addSpecialty(specialty1);
        vet.addSpecialty(specialty2);

        List<Specialty> sortedSpecialties = vet.getSpecialties();
        assertEquals(2, sortedSpecialties.size());
        assertEquals("Dentistry", sortedSpecialties.get(0).getName()); // Kiểm tra sắp xếp
        assertEquals("Surgery", sortedSpecialties.get(1).getName());
    }

    @Test
    void testGetSpecialtiesWithEmptyList() {
        Vet vet = new Vet();
        List<Specialty> specialties = vet.getSpecialties();
        assertTrue(specialties.isEmpty());
    }
}
