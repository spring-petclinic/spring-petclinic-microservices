package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;

import static org.assertj.core.api.Assertions.assertThat;

public class OwnerTest {

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");
    }

    @Test
    void testGetters() {
        assertThat(owner.getFirstName()).isEqualTo("John");
        assertThat(owner.getLastName()).isEqualTo("Doe");
        assertThat(owner.getAddress()).isEqualTo("123 Main St");
        assertThat(owner.getCity()).isEqualTo("Springfield");
        assertThat(owner.getTelephone()).isEqualTo("1234567890");
    }

    @Test
    void testSetters() {
        owner.setFirstName("Alice");
        owner.setLastName("Smith");
        owner.setAddress("456 Oak Ave");
        owner.setCity("Shelbyville");
        owner.setTelephone("0987654321");

        assertThat(owner.getFirstName()).isEqualTo("Alice");
        assertThat(owner.getLastName()).isEqualTo("Smith");
        assertThat(owner.getAddress()).isEqualTo("456 Oak Ave");
        assertThat(owner.getCity()).isEqualTo("Shelbyville");
        assertThat(owner.getTelephone()).isEqualTo("0987654321");
    }

    @Test
    void testAddPet() {
        Pet pet = new Pet();
        pet.setName("Fluffy");

        owner.addPet(pet);

        assertThat(owner.getPets()).hasSize(1);
        assertThat(owner.getPets().get(0).getName()).isEqualTo("Fluffy");
        assertThat(owner.getPets().get(0).getOwner()).isEqualTo(owner);
    }

    @Test
    void testGetPetsReturnsSortedList() {
        Pet pet1 = new Pet();
        pet1.setName("Charlie");

        Pet pet2 = new Pet();
        pet2.setName("Bella");

        owner.addPet(pet1);
        owner.addPet(pet2);

        assertThat(owner.getPets()).extracting("name").containsExactly("Bella", "Charlie");
    }

    @Test
    void testToString() {
        owner.setId(1);
        String result = owner.toString();

        assertThat(result)
            .contains("id = 1")
            .contains("firstName = 'John'")
            .contains("lastName = 'Doe'")
            .contains("address = '123 Main St'")
            .contains("city = 'Springfield'")
            .contains("telephone = '1234567890'");
    }
}
