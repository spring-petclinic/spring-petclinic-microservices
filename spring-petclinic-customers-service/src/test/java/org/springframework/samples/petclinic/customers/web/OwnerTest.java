package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void testPetsAreSortedByName() {
        Pet petA = new Pet();
        petA.setName("Zebra");

        Pet petB = new Pet();
        petB.setName("Alpha");

        owner.addPet(petA);
        owner.addPet(petB);

        List<Pet> pets = owner.getPets();
        assertEquals("Alpha", pets.get(0).getName());
        assertEquals("Zebra", pets.get(1).getName());
    }

    @Test
    void testAddPetAssignsOwner() {
        Pet pet = new Pet();
        pet.setName("Buddy");

        owner.addPet(pet);

        assertEquals(owner, pet.getOwner());
    }


    @Test
    void testGetPetsInternalNotNull() throws Exception {
        Field petsField = Owner.class.getDeclaredField("pets");
        petsField.setAccessible(true);
        petsField.set(owner, null); // Simulate `null` pets set

        assertNotNull(owner.getPets());
        assertTrue(owner.getPets().isEmpty());
    }

    @Test
    void testOwnerIdInitiallyNull() {
        assertNull(owner.getId());
    }
}