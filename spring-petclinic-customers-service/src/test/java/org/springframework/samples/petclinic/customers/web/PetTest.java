package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetTest {

    @Test
    void testGettersAndSetters() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(new Date());
        PetType type = new PetType();
        type.setName("Dog");
        pet.setType(type);
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        pet.setOwner(owner);

        assertEquals(1, pet.getId());
        assertEquals("Buddy", pet.getName());
        assertNotNull(pet.getBirthDate());
        assertEquals("Dog", pet.getType().getName());
        assertEquals("John", pet.getOwner().getFirstName());
        assertEquals("Doe", pet.getOwner().getLastName());
    }

    @Test
    void testToString() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(new Date());
        PetType type = new PetType();
        type.setName("Dog");
        pet.setType(type);
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        pet.setOwner(owner);

        String toString = pet.toString();
        assertFalse(toString.contains("id=1"));
    }

    @Test
    void testEqualsAndHashCode() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Buddy");

        assertEquals(pet1, pet2);
        assertEquals(pet1.hashCode(), pet2.hashCode());

        pet2.setName("Max");
        assertNotEquals(pet1, pet2);
    }

    @Test
    void testEquals_NullObject_ShouldReturnFalse() {
        Pet pet = new Pet();
        assertFalse(pet.equals(null));
    }

    @Test
    void testEquals_DifferentClass_ShouldReturnFalse() {
        Pet pet = new Pet();
        assertFalse(pet.equals("Not a Pet"));
    }

    @Test
    void testEquals_DifferentId_ShouldReturnFalse() {
        Pet pet1 = new Pet();
        pet1.setId(1);

        Pet pet2 = new Pet();
        pet2.setId(2);

        assertFalse(pet1.equals(pet2));
    }

    @Test
    void testEquals_DifferentName_ShouldReturnFalse() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Max");

        assertFalse(pet1.equals(pet2));
    }

    @Test
    void testEquals_DifferentBirthDate_ShouldReturnFalse() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");
        pet1.setBirthDate(new Date(2020, 1, 1));

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Buddy");
        pet2.setBirthDate(new Date(2021, 1, 1));

        assertFalse(pet1.equals(pet2));
    }

    @Test
    void testEquals_DifferentType_ShouldReturnFalse() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");
        pet1.setBirthDate(new Date(2020, 1, 1));
        PetType type1 = new PetType();
        type1.setName("Dog");
        pet1.setType(type1);

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Buddy");
        pet2.setBirthDate(new Date(2020, 1, 1));
        PetType type2 = new PetType();
        type2.setName("Cat");
        pet2.setType(type2);

        assertFalse(pet1.equals(pet2));
    }

    @Test
    void testEquals_DifferentOwner_ShouldReturnFalse() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");
        pet1.setBirthDate(new Date(2020, 1, 1));
        PetType type = new PetType();
        type.setName("Dog");
        pet1.setType(type);
        Owner owner1 = new Owner();
        owner1.setFirstName("John");
        pet1.setOwner(owner1);

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Buddy");
        pet2.setBirthDate(new Date(2020, 1, 1));
        pet2.setType(type);
        Owner owner2 = new Owner();
        owner2.setFirstName("Jane");
        pet2.setOwner(owner2);

        assertFalse(pet1.equals(pet2));
    }

    @Test
    void testEquals_AllFieldsEqual_ShouldReturnTrue() {
        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Buddy");
        pet1.setBirthDate(new Date(2020, 1, 1));
        PetType type = new PetType();
        type.setName("Dog");
        pet1.setType(type);
        Owner owner = new Owner();
        owner.setFirstName("John");
        pet1.setOwner(owner);

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Buddy");
        pet2.setBirthDate(new Date(2020, 1, 1));
        pet2.setType(type);
        pet2.setOwner(owner);

        assertTrue(pet1.equals(pet2));
    }
}