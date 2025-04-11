package org.springframework.samples.petclinic.vets.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;

import jakarta.transaction.Transactional;
@SpringBootTest
@Transactional

public class VetRepositoryTest {
    @Autowired
    private VetRepository vetRepository;

    @Test
    void testSaveAndFindById() {
        Vet vet = new Vet();
        vet.setFirstName("Emily");
        vet.setLastName("Nguyen");

        Specialty specialty = new Specialty();
        specialty.setName("Cardiology");

        vet.addSpecialty(specialty);

        Vet savedVet = vetRepository.save(vet);

        Optional<Vet> retrieved = vetRepository.findById(savedVet.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Emily", retrieved.get().getFirstName());
        assertEquals("Nguyen", retrieved.get().getLastName());
        assertEquals(1, retrieved.get().getNrOfSpecialties());
        
    }

    @Test
    void testFindAll() {
        Vet vet1 = new Vet();
        vet1.setFirstName("Anna");
        vet1.setLastName("Smith");

        Vet vet2 = new Vet();
        vet2.setFirstName("John");
        vet2.setLastName("Doe");

        vetRepository.save(vet1);
        vetRepository.save(vet2);

        assertEquals(2, vetRepository.findAll().size());
    }
}
