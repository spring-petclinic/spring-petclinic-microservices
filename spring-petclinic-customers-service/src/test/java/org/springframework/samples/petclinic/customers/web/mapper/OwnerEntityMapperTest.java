package org.springframework.samples.petclinic.customers.web.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;

import static org.junit.jupiter.api.Assertions.*;

class OwnerEntityMapperTest {

    private OwnerEntityMapper mapper;
    private Owner existingOwner;
    private OwnerRequest request;

    @BeforeEach
    void setUp() {
        mapper = new OwnerEntityMapper();

        // Create an existing owner with initial values
        existingOwner = new Owner();
        existingOwner.setFirstName("John");
        existingOwner.setLastName("Doe");
        existingOwner.setAddress("123 Main St");
        existingOwner.setCity("New York");
        existingOwner.setTelephone("1234567890");
    }

    @Test
    @DisplayName("Should map OwnerRequest to existing Owner")
    void shouldMapOwnerRequestToExistingOwner() {
        // Given
        OwnerRequest request = new OwnerRequest(
            "Jane",
            "Smith",
            "456 Oak St",
            "Boston",
            "9876543210"
        );

        // When
        Owner mappedOwner = mapper.map(existingOwner, request);

        // Then
        assertSame(existingOwner, mappedOwner, "Should return the same owner instance");
        assertEquals("Jane", mappedOwner.getFirstName());
        assertEquals("Smith", mappedOwner.getLastName());
        assertEquals("456 Oak St", mappedOwner.getAddress());
        assertEquals("Boston", mappedOwner.getCity());
        assertEquals("9876543210", mappedOwner.getTelephone());
    }

    @Test
    @DisplayName("Should map OwnerRequest to new Owner")
    void shouldMapOwnerRequestToNewOwner() {
        // Given
        Owner newOwner = new Owner();
        request = new OwnerRequest(
            "Jane",
            "Smith",
            "456 Oak St",
            "Boston",
            "9876543210"
        );

        // When
        Owner mappedOwner = mapper.map(newOwner, request);

        // Then
        assertSame(newOwner, mappedOwner, "Should return the same owner instance");
        assertEquals("Jane", mappedOwner.getFirstName());
        assertEquals("Smith", mappedOwner.getLastName());
        assertEquals("456 Oak St", mappedOwner.getAddress());
        assertEquals("Boston", mappedOwner.getCity());
        assertEquals("9876543210", mappedOwner.getTelephone());
        assertNull(mappedOwner.getId(), "ID should remain null for new owner");
    }

    @Test
    @DisplayName("Should correctly handle null values in OwnerRequest")
    void shouldHandleNullValuesInOwnerRequest() {
        // Given
        request = new OwnerRequest(
            null,
            null,
            null,
            null,
            null
        );

        // When
        Owner mappedOwner = mapper.map(existingOwner, request);

        // Then
        assertSame(existingOwner, mappedOwner, "Should return the same owner instance");
        assertNull(mappedOwner.getFirstName());
        assertNull(mappedOwner.getLastName());
        assertNull(mappedOwner.getAddress());
        assertNull(mappedOwner.getCity());
        assertNull(mappedOwner.getTelephone());
    }

    @Test
    @DisplayName("Should not affect pets when mapping OwnerRequest")
    void shouldNotAffectPetsWhenMappingOwnerRequest() {
        // Given
        Owner ownerWithPets = new Owner();
        ownerWithPets.setFirstName("John");
        ownerWithPets.setLastName("Doe");

        // Add some pets to the owner
        Pet mockPet = new Pet();
        mockPet.setName("Buddy");
        ownerWithPets.addPet(mockPet);

        OwnerRequest request = new OwnerRequest(
            "Jane",
            "Smith",
            "456 Oak St",
            "Boston",
            "9876543210"
        );

        // When
        Owner mappedOwner = mapper.map(ownerWithPets, request);

        // Then
        assertEquals(1, mappedOwner.getPets().size(), "Pet count should remain unchanged");
        assertEquals("Buddy", mappedOwner.getPets().get(0).getName(), "Pet details should remain unchanged");
    }

    @Test
    @DisplayName("Should preserve empty strings in OwnerRequest")
    void shouldPreserveEmptyStringsInOwnerRequest() {
        // Given
        OwnerRequest request = new OwnerRequest(
            "",
            "",
            "",
            "",
            ""
        );

        // When
        Owner mappedOwner = mapper.map(existingOwner, request);

        // Then
        assertSame(existingOwner, mappedOwner, "Should return the same owner instance");
        assertEquals("", mappedOwner.getFirstName());
        assertEquals("", mappedOwner.getLastName());
        assertEquals("", mappedOwner.getAddress());
        assertEquals("", mappedOwner.getCity());
        assertEquals("", mappedOwner.getTelephone());
    }
}
