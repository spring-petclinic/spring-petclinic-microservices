package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetTest {

    @Test
    void testPetSettersAndGetters() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(new Date());

        PetType type = new PetType();
        type.setId(2);
        type.setName("Dog");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setFirstName("John");
        pet.setOwner(owner);

        assertEquals(1, pet.getId());
        assertEquals("Buddy", pet.getName());
        assertNotNull(pet.getBirthDate());
        assertEquals("Dog", pet.getType().getName());
        assertEquals("John", pet.getOwner().getFirstName());
    }

    @Test
    void testPetEqualsAndHashCode() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Max");

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Max");

        assertEquals(pet1, pet2);
        assertEquals(pet1.hashCode(), pet2.hashCode());
    }

    @Test
    void testPetEqualsWithDifferentObjects() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");

        Pet pet2 = new Pet();
        pet2.setId(2);
        pet2.setName("Buddy");

        assertNotEquals(pet1, pet2);
    }

    @Test
    void testPetEqualsWithNull() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");

        assertNotEquals(pet, null);
    }

    @Test
    void testPetEqualsWithDifferentClass() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");

        String differentObject = "This is a string";
        assertNotEquals(pet, differentObject);
    }

    @Test
    void testPetHashCodeWithDifferentObjects() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");

        Pet pet2 = new Pet();
        pet2.setId(2);
        pet2.setName("Buddy");

        assertNotEquals(pet1.hashCode(), pet2.hashCode());
    }
}
