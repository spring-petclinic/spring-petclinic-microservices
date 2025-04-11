package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetTest {

    private Pet pet;
    private Owner owner;
    private PetType petType;
    private Date birthDate;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        petType = new PetType();
        petType.setId(1);
        petType.setName("dog");

        birthDate = new Date();

        pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(birthDate);
        pet.setType(petType);
        pet.setOwner(owner);
    }

    @Test
    @DisplayName("Should get and set id correctly")
    void shouldGetAndSetIdCorrectly() {
        // When
        pet.setId(2);

        // Then
        assertEquals(2, pet.getId());
    }

    @Test
    @DisplayName("Should get and set name correctly")
    void shouldGetAndSetNameCorrectly() {
        // When
        pet.setName("Max");

        // Then
        assertEquals("Max", pet.getName());
    }

    @Test
    @DisplayName("Should get and set birthDate correctly")
    void shouldGetAndSetBirthDateCorrectly() {
        // Given
        Date newDate = new Date(birthDate.getTime() + 86400000); // Add a day

        // When
        pet.setBirthDate(newDate);

        // Then
        assertEquals(newDate, pet.getBirthDate());
    }

    @Test
    @DisplayName("Should get and set type correctly")
    void shouldGetAndSetTypeCorrectly() {
        // Given
        PetType newType = new PetType();
        newType.setId(2);
        newType.setName("cat");

        // When
        pet.setType(newType);

        // Then
        assertEquals(newType, pet.getType());
        assertEquals("cat", pet.getType().getName());
    }

    @Test
    @DisplayName("Should get and set owner correctly")
    void shouldGetAndSetOwnerCorrectly() {
        // Given
        Owner newOwner = new Owner();
        newOwner.setFirstName("Jane");
        newOwner.setLastName("Smith");

        // When
        pet.setOwner(newOwner);

        // Then
        assertEquals(newOwner, pet.getOwner());
        assertEquals("Jane", pet.getOwner().getFirstName());
    }

    @Test
    @DisplayName("Should generate correct toString output")
    void shouldGenerateCorrectToStringOutput() {
        // When
        String toString = pet.toString();
        System.out.println(toString);
        // Then
        assertTrue(toString.contains("id = 1"));
        assertTrue(toString.contains("name = 'Buddy'"));
        assertTrue(toString.contains("type = 'dog'"));
        assertTrue(toString.contains("ownerFirstname = 'John'"));
        assertTrue(toString.contains("ownerLastname = 'Doe'"));
    }

    @Test
    @DisplayName("Should correctly implement equals and hashCode")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        Pet samePet = new Pet();
        samePet.setId(1);
        samePet.setName("Buddy");
        samePet.setBirthDate(birthDate);
        samePet.setType(petType);
        samePet.setOwner(owner);

        Pet differentPet = new Pet();
        differentPet.setId(2);
        differentPet.setName("Max");
        differentPet.setBirthDate(birthDate);
        differentPet.setType(petType);
        differentPet.setOwner(owner);

        // Then
        assertEquals(pet, samePet);
        assertEquals(pet.hashCode(), samePet.hashCode());
        assertNotEquals(pet, differentPet);
        assertNotEquals(pet.hashCode(), differentPet.hashCode());
    }

    @Test
    @DisplayName("Should return false when comparing to null")
    void shouldReturnFalseWhenComparingToNull() {
        // Then
        assertFalse(pet.equals(null));
    }

    @Test
    @DisplayName("Should return false when comparing to different type")
    void shouldReturnFalseWhenComparingToDifferentType() {
        // Then
        assertFalse(pet.equals("Not a Pet"));
    }
}
