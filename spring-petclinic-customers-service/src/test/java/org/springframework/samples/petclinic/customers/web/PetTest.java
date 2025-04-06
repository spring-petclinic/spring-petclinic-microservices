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
}