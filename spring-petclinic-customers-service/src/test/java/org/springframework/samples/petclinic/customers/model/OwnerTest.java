package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OwnerTest {

    private Owner owner;
    private Pet pet1;
    private Pet pet2;
    private PetType petType;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("New York");
        owner.setTelephone("1234567890");

        petType = new PetType();
        petType.setId(1);
        petType.setName("dog");

        pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");
        pet1.setBirthDate(new Date());
        pet1.setType(petType);

        pet2 = new Pet();
        pet2.setId(2);
        pet2.setName("Max");
        pet2.setBirthDate(new Date());
        pet2.setType(petType);
    }

    @Test
    @DisplayName("Should add pet to owner")
    void shouldAddPetToOwner() {
        // When.
        owner.addPet(pet1);

        // Then
        List<Pet> pets = owner.getPets();
        assertEquals(1, pets.size());
        assertEquals("Buddy", pets.get(0).getName());
        assertEquals(owner, pet1.getOwner());
    }

    @Test
    @DisplayName("Should return pets in alphabetical order by name")
    void shouldReturnPetsInAlphabeticalOrder() {
        // Given
        pet1.setName("Zebra");
        pet2.setName("Alpha");

        // When
        owner.addPet(pet1);
        owner.addPet(pet2);

        // Then
        List<Pet> pets = owner.getPets();
        assertEquals(2, pets.size());
        assertEquals("Alpha", pets.get(0).getName());
        assertEquals("Zebra", pets.get(1).getName());
    }

    @Test
    @DisplayName("Should return empty list when no pets")
    void shouldReturnEmptyListWhenNoPets() {
        // When
        List<Pet> pets = owner.getPets();

        // Then
        assertEquals(0, pets.size());
    }

    @Test
    @DisplayName("Should initialize pets set if null")
    void shouldInitializePetsSetIfNull() {
        // When/Then
        assertNotNull(owner.getPets());
    }

    @Test
    @DisplayName("Should generate correct toString output")
    void shouldGenerateCorrectToStringOutput() {
        String toString = owner.toString();

        assertTrue(toString.contains("lastName = 'Doe'"));
        assertTrue(toString.contains("firstName = 'John'"));
        assertTrue(toString.contains("address = '123 Main St'"));
        assertTrue(toString.contains("city = 'New York'"));
        assertTrue(toString.contains("telephone = '1234567890'"));
    }


    @Test
    @DisplayName("Should set and get firstName correctly")
    void shouldSetAndGetFirstNameCorrectly() {
        // When
        owner.setFirstName("Jane");

        // Then
        assertEquals("Jane", owner.getFirstName());
    }

    @Test
    @DisplayName("Should set and get lastName correctly")
    void shouldSetAndGetLastNameCorrectly() {
        // When
        owner.setLastName("Smith");

        // Then
        assertEquals("Smith", owner.getLastName());
    }

    @Test
    @DisplayName("Should set and get address correctly")
    void shouldSetAndGetAddressCorrectly() {
        // When
        owner.setAddress("456 Oak St");

        // Then
        assertEquals("456 Oak St", owner.getAddress());
    }

    @Test
    @DisplayName("Should set and get city correctly")
    void shouldSetAndGetCityCorrectly() {
        // When
        owner.setCity("Boston");

        // Then
        assertEquals("Boston", owner.getCity());
    }

    @Test
    @DisplayName("Should set and get telephone correctly")
    void shouldSetAndGetTelephoneCorrectly() {
        // When
        owner.setTelephone("9876543210");

        // Then
        assertEquals("9876543210", owner.getTelephone());
    }

}
