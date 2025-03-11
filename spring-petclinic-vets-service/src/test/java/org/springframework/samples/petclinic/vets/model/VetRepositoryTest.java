package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class VetRepositoryTest {

    @Autowired
    private VetRepository vetRepository;

    @Test
    void testSaveAndFindVet() {
        Vet vet = new Vet();
        vet.setFirstName("Alice");
        vet.setLastName("Brown");

        Vet savedVet = vetRepository.save(vet);
        Optional<Vet> foundVet = vetRepository.findById(savedVet.getId());

        assertTrue(foundVet.isPresent(), "Vet should be found");
        assertEquals("Alice", foundVet.get().getFirstName(), "First name should match");
        assertEquals("Brown", foundVet.get().getLastName(), "Last name should match");
    }

    @Test
    void testDeleteVet() {
        Vet vet = new Vet();
        vet.setFirstName("Bob");
        vet.setLastName("Smith");

        Vet savedVet = vetRepository.save(vet);
        vetRepository.delete(savedVet);

        Optional<Vet> foundVet = vetRepository.findById(savedVet.getId());
        assertFalse(foundVet.isPresent(), "Vet should be deleted");
    }
}
