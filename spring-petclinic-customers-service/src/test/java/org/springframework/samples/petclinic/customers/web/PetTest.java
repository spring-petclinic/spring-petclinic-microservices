package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class PetTest {

    private Pet pet;
    private Owner owner;
    private PetType petType;

    @BeforeEach
    void setUp() {
        pet = new Pet();
        owner = new Owner();
        petType = new PetType();

        owner.setFirstName("John");
        owner.setLastName("Doe");

        petType.setName("Dog");

        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(new Date(1234567890000L)); // Fixed date
        pet.setOwner(owner);
        pet.setType(petType);
    }

    @Test
    void testGetters() {
        assertThat(pet.getId()).isEqualTo(1);
        assertThat(pet.getName()).isEqualTo("Buddy");
        assertThat(pet.getBirthDate()).isEqualTo(new Date(1234567890000L));
        assertThat(pet.getOwner()).isEqualTo(owner);
        assertThat(pet.getType()).isEqualTo(petType);
    }

    @Test
    void testEqualsAndHashCode() {
        Pet anotherPet = new Pet();
        anotherPet.setId(1);
        anotherPet.setName("Buddy");
        anotherPet.setBirthDate(new Date(1234567890000L));
        anotherPet.setOwner(owner);
        anotherPet.setType(petType);

        assertThat(pet).isEqualTo(anotherPet);
        assertThat(pet.hashCode()).isEqualTo(anotherPet.hashCode());
    }

    @Test
    void testToString() {
        String result = pet.toString();

        assertThat(result)
            .contains("id = 1")
            .contains("name = 'Buddy'")
            .contains("birthDate =")
            .contains("type = 'Dog'")
            .contains("ownerFirstname = 'John'")
            .contains("ownerLastname = 'Doe'");
    }
}
