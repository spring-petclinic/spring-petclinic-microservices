package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    private Vet vet;
    private Specialty specialty1;
    private Specialty specialty2;

    @BeforeEach
    void setUp() {
        vet = new Vet();
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        specialty1 = new Specialty();
        specialty1.setName("Radiology");

        specialty2 = new Specialty();
        specialty2.setName("Surgery");
    }

    @Test
    void testGetId() {
        assertEquals(Integer.valueOf(1), vet.getId());
    }

    @Test
    void testSetId() {
        vet.setId(2);
        assertEquals(Integer.valueOf(2), vet.getId());
    }

    @Test
    void testGetFirstName() {
        assertEquals("John", vet.getFirstName());
    }

    @Test
    void testSetFirstName() {
        vet.setFirstName("Jane");
        assertEquals("Jane", vet.getFirstName());
    }

    @Test
    void testGetLastName() {
        assertEquals("Doe", vet.getLastName());
    }

    @Test
    void testSetLastName() {
        vet.setLastName("Smith");
        assertEquals("Smith", vet.getLastName());
    }

    @Test
    void testGetSpecialtiesInternal_WhenNull() {
        // Access internal specialties when null
        Vet newVet = new Vet();
        Set<Specialty> specialties = newVet.getSpecialtiesInternal();
        
        assertNotNull(specialties);
        assertTrue(specialties.isEmpty());
    }

    @Test
    void testGetSpecialtiesInternal_WhenNotNull() {
        vet.addSpecialty(specialty1);
        Set<Specialty> specialties = vet.getSpecialtiesInternal();
        
        assertNotNull(specialties);
        assertEquals(1, specialties.size());
        assertTrue(specialties.contains(specialty1));
    }

    @Test
    void testGetSpecialties() {
        vet.addSpecialty(specialty2);
        vet.addSpecialty(specialty1);
        
        List<Specialty> specialties = vet.getSpecialties();
        
        assertNotNull(specialties);
        assertEquals(2, specialties.size());
        
        // Verify sorted by name (Radiology should come before Surgery)
        assertEquals("Radiology", specialties.get(0).getName());
        assertEquals("Surgery", specialties.get(1).getName());
    }

    @Test
    void testGetNrOfSpecialties() {
        assertEquals(0, vet.getNrOfSpecialties());
        
        vet.addSpecialty(specialty1);
        assertEquals(1, vet.getNrOfSpecialties());
        
        vet.addSpecialty(specialty2);
        assertEquals(2, vet.getNrOfSpecialties());
    }

    @Test
    void testAddSpecialty() {
        assertTrue(vet.getSpecialties().isEmpty());
        
        vet.addSpecialty(specialty1);
        assertEquals(1, vet.getNrOfSpecialties());
        assertTrue(vet.getSpecialties().contains(specialty1));
        
        // Add duplicate specialty should not change count
        vet.addSpecialty(specialty1);
        assertEquals(1, vet.getNrOfSpecialties());
    }

    @Test
    void testGetSpecialties_Immutability() {
        vet.addSpecialty(specialty1);
        List<Specialty> specialties = vet.getSpecialties();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            specialties.add(specialty2);
        });
    }
}

class SpecialtyTest {

    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialty = new Specialty();
        specialty.setName("Dentistry");
    }

    @Test
    void testGetId() {
        assertNull(specialty.getId()); // ID is null until persisted
    }

    @Test
    void testGetName() {
        assertEquals("Dentistry", specialty.getName());
    }

    @Test
    void testSetName() {
        specialty.setName("Cardiology");
        assertEquals("Cardiology", specialty.getName());
    }
}

@DataJpaTest
class VetRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VetRepository vetRepository;

    private Vet vet1;
    private Vet vet2;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialty = new Specialty();
        specialty.setName("Dentistry");
        entityManager.persist(specialty);
        entityManager.flush();

        vet1 = new Vet();
        vet1.setFirstName("James");
        vet1.setLastName("Carter");
        vet1.addSpecialty(specialty);
        entityManager.persist(vet1);

        vet2 = new Vet();
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");
        entityManager.persist(vet2);

        entityManager.flush();
    }

    @Test
    void testFindAll() {
        List<Vet> vets = vetRepository.findAll();
        
        assertNotNull(vets);
        assertEquals(2, vets.size());
    }

    @Test
    void testFindById_Existing() {
        Optional<Vet> found = vetRepository.findById(vet1.getId());
        
        assertTrue(found.isPresent());
        assertEquals("James", found.get().getFirstName());
        assertEquals("Carter", found.get().getLastName());
        assertEquals(1, found.get().getNrOfSpecialties());
    }

    @Test
    void testFindById_NonExisting() {
        Optional<Vet> found = vetRepository.findById(999);
        assertFalse(found.isPresent());
    }

    @Test
    void testSave_New() {
        Vet vet3 = new Vet();
        vet3.setFirstName("Linda");
        vet3.setLastName("Douglas");
        
        Vet saved = vetRepository.save(vet3);
        
        assertNotNull(saved.getId());
        assertEquals("Linda", saved.getFirstName());
        assertEquals("Douglas", saved.getLastName());
        
        Optional<Vet> found = vetRepository.findById(saved.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void testSave_Update() {
        vet1.setFirstName("Updated");
        Vet updated = vetRepository.save(vet1);
        
        assertEquals(vet1.getId(), updated.getId());
        assertEquals("Updated", updated.getFirstName());
        
        Optional<Vet> found = vetRepository.findById(vet1.getId());
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getFirstName());
    }

    @Test
    void testDelete() {
        vetRepository.delete(vet1);
        
        Optional<Vet> found = vetRepository.findById(vet1.getId());
        assertFalse(found.isPresent());
        
        // Verify vet2 still exists
        found = vetRepository.findById(vet2.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void testSpecialtySorting() {
        // Create vets with multiple specialties in non-alphabetical order
        Specialty radiology = new Specialty();
        radiology.setName("Radiology");
        entityManager.persist(radiology);
        
        Specialty surgery = new Specialty();
        surgery.setName("Surgery");
        entityManager.persist(surgery);
        
        Vet vetWithMultipleSpecialties = new Vet();
        vetWithMultipleSpecialties.setFirstName("John");
        vetWithMultipleSpecialties.setLastName("Smith");
        // Add in non-alphabetical order
        vetWithMultipleSpecialties.addSpecialty(surgery);
        vetWithMultipleSpecialties.addSpecialty(specialty); // Dentistry
        vetWithMultipleSpecialties.addSpecialty(radiology);
        
        entityManager.persist(vetWithMultipleSpecialties);
        entityManager.flush();
        
        Optional<Vet> found = vetRepository.findById(vetWithMultipleSpecialties.getId());
        assertTrue(found.isPresent());
        
        List<Specialty> specialties = found.get().getSpecialties();
        assertEquals(3, specialties.size());
        
        // Verify alphabetical order
        assertEquals("Dentistry", specialties.get(0).getName());
        assertEquals("Radiology", specialties.get(1).getName());
        assertEquals("Surgery", specialties.get(2).getName());
    }
}

@DataJpaTest
class SpecialtyJpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testPersistSpecialty() {
        Specialty specialty = new Specialty();
        specialty.setName("Neurology");
        
        entityManager.persist(specialty);
        entityManager.flush();
        
        assertNotNull(specialty.getId());
        
        Specialty found = entityManager.find(Specialty.class, specialty.getId());
        assertNotNull(found);
        assertEquals("Neurology", found.getName());
    }

    @Test
    void testUpdateSpecialty() {
        // Create and persist a specialty
        Specialty specialty = new Specialty();
        specialty.setName("Dermatology");
        entityManager.persist(specialty);
        entityManager.flush();
        
        // Update the specialty
        specialty.setName("Updated Dermatology");
        entityManager.merge(specialty);
        entityManager.flush();
        
        // Verify the update
        Specialty found = entityManager.find(Specialty.class, specialty.getId());
        assertEquals("Updated Dermatology", found.getName());
    }

    @Test
    void testRemoveSpecialty() {
        // Create and persist a specialty
        Specialty specialty = new Specialty();
        specialty.setName("Ophthalmology");
        entityManager.persist(specialty);
        entityManager.flush();
        
        Integer id = specialty.getId();
        
        // Remove the specialty
        entityManager.remove(specialty);
        entityManager.flush();
        
        // Verify removal
        Specialty found = entityManager.find(Specialty.class, id);
        assertNull(found);
    }
}
