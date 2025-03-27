package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Date;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PetTest {

    private Pet pet;
    private Owner owner;
    private PetType type;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        type = new PetType();
        type.setName("Dog");

        pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(new Date());
        pet.setType(type);
        pet.setOwner(owner);
    }

    @Test
    void testGetId() {
        assertThat(pet.getId()).isEqualTo(1);
    }

    @Test
    void testGetName() {
        assertThat(pet.getName()).isEqualTo("Buddy");
    }

    @Test
    void testGetBirthDate() {
        assertThat(pet.getBirthDate()).isNotNull();
    }

    @Test
    void testGetType() {
        assertThat(pet.getType()).isEqualTo(type);
        assertThat(pet.getType().getName()).isEqualTo("Dog");
    }

    @Test
    void testGetOwner() {
        assertThat(pet.getOwner()).isEqualTo(owner);
        assertThat(pet.getOwner().getFirstName()).isEqualTo("John");
        assertThat(pet.getOwner().getLastName()).isEqualTo("Doe");
    }

    @Test
    void testEqualsAndHashCode() {
        Pet anotherPet = new Pet();
        anotherPet.setId(1);
        anotherPet.setName("Buddy");
        anotherPet.setBirthDate(pet.getBirthDate());
        anotherPet.setType(type);
        anotherPet.setOwner(owner);

        assertThat(pet).isEqualTo(anotherPet);
        assertThat(pet.hashCode()).isEqualTo(anotherPet.hashCode());
    }

    @Test
    void testSetName() {
        pet.setName("Charlie");
        assertThat(pet.getName()).isEqualTo("Charlie");
    }

    @Test
    void testSetBirthDate() {
        Date newDate = new Date(System.currentTimeMillis() - 86400000L); // Yesterday
        pet.setBirthDate(newDate);
        assertThat(pet.getBirthDate()).isEqualTo(newDate);
    }

    @Test
    void testSetType() {
        PetType newType = new PetType();
        newType.setName("Cat");
        pet.setType(newType);

        assertThat(pet.getType()).isEqualTo(newType);
        assertThat(pet.getType().getName()).isEqualTo("Cat");
    }

    @Test
    void testSetOwner() {
        Owner newOwner = new Owner();
        newOwner.setFirstName("Jane");
        newOwner.setLastName("Smith");

        pet.setOwner(newOwner);

        assertThat(pet.getOwner()).isEqualTo(newOwner);
        assertThat(pet.getOwner().getFirstName()).isEqualTo("Jane");
        assertThat(pet.getOwner().getLastName()).isEqualTo("Smith");
    }

    @Test
    void testNullOwner() {
        pet.setOwner(null);
        assertNull(pet.getOwner());
    }

    @Test
    void testNullType() {
        pet.setType(null);
        assertNull(pet.getType());
    }

    @Test
    void testEqualsWithDifferentId() {
        Pet anotherPet = new Pet();
        anotherPet.setId(2); // Different ID
        anotherPet.setName("Buddy");
        anotherPet.setBirthDate(pet.getBirthDate());
        anotherPet.setType(type);
        anotherPet.setOwner(owner);

        assertNotEquals(pet, anotherPet);
    }

    @Test
    void testEqualsWithDifferentName() {
        Pet anotherPet = new Pet();
        anotherPet.setId(1);
        anotherPet.setName("Max"); // Different name
        anotherPet.setBirthDate(pet.getBirthDate());
        anotherPet.setType(type);
        anotherPet.setOwner(owner);

        assertNotEquals(pet, anotherPet);
    }

    @Test
    void testEqualsWithNull() {
        assertNotEquals(null, pet);
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertNotEquals(pet, "Some String");
    }
}
