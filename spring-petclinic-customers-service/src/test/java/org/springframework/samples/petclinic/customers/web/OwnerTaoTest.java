package org.springframework.samples.petclinic.customers.web;
import org.springframework.samples.petclinic.customers.model.*;


import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OwnerTaoTest {
    @Test
    void testGetPetsInternal_WhenPetsIsNull_ShouldInitializePets() throws Exception {
        Owner owner = new Owner();

        // Use reflection to access the protected method
        var method = Owner.class.getDeclaredMethod("getPetsInternal");
        method.setAccessible(true);

        // Call the method and verify the result
        @SuppressWarnings("unchecked")
        Set<Pet> pets = (Set<Pet>) method.invoke(owner);
        assertNotNull(pets);
        assertTrue(pets.isEmpty());
    }

    @Test
    void testGetPetsInternal_WhenPetsIsNotNull_ShouldReturnExistingPets() throws Exception {
        Owner owner = new Owner();

        // Use reflection to access the protected method
        var method = Owner.class.getDeclaredMethod("getPetsInternal");
        method.setAccessible(true);

        // Add a pet to the internal set
        @SuppressWarnings("unchecked")
        Set<Pet> pets = (Set<Pet>) method.invoke(owner);
        Pet pet = new Pet();
        pets.add(pet);

        // Call the method again and verify the result
        @SuppressWarnings("unchecked")
        Set<Pet> result = (Set<Pet>) method.invoke(owner);
        assertEquals(1, result.size());
        assertTrue(result.contains(pet));
    }
}