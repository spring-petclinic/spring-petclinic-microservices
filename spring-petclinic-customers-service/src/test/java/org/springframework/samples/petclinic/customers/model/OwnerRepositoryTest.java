package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class OwnerRepositoryTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testSaveAndFindOwner() {
        Owner owner = new Owner();
        owner.setFirstName("Alice");
        owner.setLastName("Brown");
        owner.setAddress("456 Elm St");
        owner.setCity("San Francisco");
        owner.setTelephone("9876543210");

        Owner savedOwner = ownerRepository.save(owner);
        Optional<Owner> foundOwner = ownerRepository.findById(savedOwner.getId());

        assertTrue(foundOwner.isPresent());
        assertEquals("Alice", foundOwner.get().getFirstName());
    }

    @Test
    void testDeleteOwner() {
        Owner owner = new Owner();
        owner.setFirstName("Bob");
        owner.setLastName("Smith");
        owner.setAddress("789 Oak St");
        owner.setCity("Los Angeles");
        owner.setTelephone("5678901234");

        Owner savedOwner = ownerRepository.save(owner);
        ownerRepository.delete(savedOwner);

        Optional<Owner> foundOwner = ownerRepository.findById(savedOwner.getId());
        assertFalse(foundOwner.isPresent());
    }
}
