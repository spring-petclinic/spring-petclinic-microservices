package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class PetRepositoryTest {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TestEntityManager entityManager; // ✅ Use TestEntityManager for PetType

    @Test
    void testSaveAndFindPet() {
        Pet pet = new Pet();
        pet.setName("Charlie");

        PetType type = new PetType();
        type.setName("Dog");
        entityManager.persist(type); // ✅ Persist `PetType` separately

        pet.setType(type);
        Pet savedPet = petRepository.save(pet);
        Optional<Pet> foundPet = petRepository.findById(savedPet.getId());

        assertTrue(foundPet.isPresent());
        assertEquals("Charlie", foundPet.get().getName());
    }

    @Test
    void testFindPetTypes() {
        PetType type1 = new PetType();
        type1.setName("Rabbit");

        PetType type2 = new PetType();
        type2.setName("Bird");

        entityManager.persist(type1); // ✅ Use entityManager instead of petRepository
        entityManager.persist(type2);
        entityManager.flush();

        List<PetType> petTypes = petRepository.findPetTypes();
        assertTrue(petTypes.size() >= 2);
    }

    @Test
    void testFindPetTypeById() {
        PetType type = new PetType();
        type.setName("Fish");
        entityManager.persist(type); // ✅ Use entityManager for saving `PetType`
        entityManager.flush();

        Optional<PetType> foundType = petRepository.findPetTypeById(type.getId());
        assertTrue(foundType.isPresent());
        assertEquals("Fish", foundType.get().getName());
    }

    @Test
    void testDeletePet() {
        Pet pet = new Pet();
        pet.setName("Milo");

        Pet savedPet = petRepository.save(pet);
        petRepository.delete(savedPet);

        Optional<Pet> foundPet = petRepository.findById(savedPet.getId());
        assertFalse(foundPet.isPresent());
    }
}
