package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetTest {

    private Pet pet;
    private Owner owner;
    private PetType type;
    private Date birthDate;

    @BeforeEach
    void setUp() {
        pet = new Pet();
        owner = new Owner();
        type = new PetType();
        birthDate = new Date();

        owner.setFirstName("John");
        owner.setLastName("Doe");
        type.setName("cat");
    }

    @Test
    void testGettersAndSetters() {
        Integer id = 1;
        String name = "Leo";

        pet.setId(id);
        pet.setName(name);
        pet.setBirthDate(birthDate);
        pet.setType(type);
        pet.setOwner(owner);

        assertEquals(id, pet.getId());
        assertEquals(name, pet.getName());
        assertEquals(birthDate, pet.getBirthDate());
        assertEquals(type, pet.getType());
        assertEquals(owner, pet.getOwner());
    }

    @Test
    void testEquals() {
        Pet pet1 = new Pet();
        Pet pet2 = new Pet();

        // Both pets are equal when they have the same properties
        pet1.setId(1);
        pet1.setName("Leo");
        pet1.setBirthDate(birthDate);
        pet1.setType(type);
        pet1.setOwner(owner);

        pet2.setId(1);
        pet2.setName("Leo");
        pet2.setBirthDate(birthDate);
        pet2.setType(type);
        pet2.setOwner(owner);

        assertTrue(pet1.equals(pet2));
        assertTrue(pet2.equals(pet1));
        assertEquals(pet1.hashCode(), pet2.hashCode());

        // Pets are not equal when they have different IDs
        pet2.setId(2);
        assertFalse(pet1.equals(pet2));
        assertFalse(pet2.equals(pet1));
        assertNotEquals(pet1.hashCode(), pet2.hashCode());
    }

    @Test
    void testToString() {
        Pet pet = setupPet();
        String toString = pet.toString();
        System.out.println("Actual toString output: " + toString);
        assertTrue(toString.contains("id = 1"));
        assertTrue(toString.contains("name = 'Leo'"));
        assertTrue(toString.contains("type = 'cat'"));
        assertTrue(toString.contains("ownerFirstname = 'John'"));
        assertTrue(toString.contains("ownerLastname = 'Doe'"));
    }

    @Test
    void testNotEqualsNull() {
        assertFalse(pet.equals(null));
    }

    @Test
    void testNotEqualsDifferentClass() {
        assertFalse(pet.equals(new Object()));
    }

    private Pet setupPet() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Leo");
        pet.setBirthDate(birthDate);
        pet.setType(type);
        pet.setOwner(owner);
        return pet;
    }
} 