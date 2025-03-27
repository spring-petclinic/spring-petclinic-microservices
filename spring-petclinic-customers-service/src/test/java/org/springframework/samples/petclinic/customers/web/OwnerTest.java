package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OwnerTest {

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Street");
        owner.setCity("New York");
        owner.setTelephone("1234567890");
    }

    @Test
    void testAddPet() {
        Pet pet = new Pet();
        pet.setName("Buddy");

        owner.addPet(pet);

        List<Pet> pets = owner.getPets();
        assertEquals(1, pets.size());
        assertEquals("Buddy", pets.get(0).getName());
        assertEquals(owner, pets.get(0).getOwner());
    }

    @Test
    void testGetPetsSorted() {
        Pet pet1 = new Pet();
        pet1.setName("Charlie");
        Pet pet2 = new Pet();
        pet2.setName("Buddy");

        owner.addPet(pet1);
        owner.addPet(pet2);

        List<Pet> pets = owner.getPets();
        assertEquals("Buddy", pets.get(0).getName());
        assertEquals("Charlie", pets.get(1).getName());
    }

    @Test
    void testOwnerDetails() {
        assertEquals("John", owner.getFirstName());
        assertEquals("Doe", owner.getLastName());
        assertEquals("123 Street", owner.getAddress());
        assertEquals("New York", owner.getCity());
        assertEquals("1234567890", owner.getTelephone());
    }
}