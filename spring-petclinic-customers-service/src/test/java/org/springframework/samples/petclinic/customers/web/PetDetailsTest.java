package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetDetailsTest {

    @Test
    void testPetDetailsConstructor() {
        PetType type = new PetType();
        type.setId(1);
        type.setName("Dog");

        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(new Date());
        pet.setType(type);
        pet.setOwner(owner);

        PetDetails petDetails = new PetDetails(pet);

        assertEquals(1, petDetails.id());
        assertEquals("Buddy", petDetails.name());
        assertEquals("John Doe", petDetails.owner());
        assertEquals("Dog", petDetails.type().getName());
    }
}
