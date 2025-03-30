package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("OwnerEntityMapper Tests")
class OwnerEntityMapperTest {

    private OwnerEntityMapper mapper;

    // Test constants to avoid magic strings
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_ADDRESS = "123 Main St";
    private static final String TEST_CITY = "Boston";
    private static final String TEST_TELEPHONE = "1234567890";

    @BeforeEach
    void setUp() {
        mapper = new OwnerEntityMapper();
    }

    @Nested
    @DisplayName("map method")
    class MapMethodTests {

        @Test
        @DisplayName("Should map OwnerRequest to new Owner entity correctly")
        void shouldMapOwnerRequestToNewOwnerEntity() {
            // Arrange
            Owner owner = new Owner();
            OwnerRequest request = new OwnerRequest(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_ADDRESS, TEST_CITY, TEST_TELEPHONE);

            // Act
            Owner result = mapper.map(owner, request);

            // Assert
            assertNotNull(result, "Mapped owner should not be null");
            assertEquals(TEST_FIRST_NAME, result.getFirstName(), "First name should be mapped correctly");
            assertEquals(TEST_LAST_NAME, result.getLastName(), "Last name should be mapped correctly");
            assertEquals(TEST_ADDRESS, result.getAddress(), "Address should be mapped correctly");
            assertEquals(TEST_CITY, result.getCity(), "City should be mapped correctly");
            assertEquals(TEST_TELEPHONE, result.getTelephone(), "Telephone should be mapped correctly");
        }

        @Test
        @DisplayName("Should update existing Owner entity with OwnerRequest values")
        void shouldUpdateExistingOwnerWithOwnerRequestValues() {
            // Arrange
            Owner existingOwner = new Owner();
            existingOwner.setFirstName("Jane");
            existingOwner.setLastName("Smith");
            existingOwner.setAddress("456 Oak St");
            existingOwner.setCity("New York");
            existingOwner.setTelephone("9876543210");

            OwnerRequest request = new OwnerRequest(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_ADDRESS, TEST_CITY, TEST_TELEPHONE);

            // Act
            Owner result = mapper.map(existingOwner, request);

            // Assert
            assertNotNull(result, "Mapped owner should not be null");
            assertEquals(TEST_FIRST_NAME, result.getFirstName(), "First name should be updated correctly");
            assertEquals(TEST_LAST_NAME, result.getLastName(), "Last name should be updated correctly");
            assertEquals(TEST_ADDRESS, result.getAddress(), "Address should be updated correctly");
            assertEquals(TEST_CITY, result.getCity(), "City should be updated correctly");
            assertEquals(TEST_TELEPHONE, result.getTelephone(), "Telephone should be updated correctly");
        }

        @Test
        @DisplayName("Should preserve non-mapped Owner properties")
        void shouldPreserveNonMappedOwnerProperties() {
            // Arrange
            Owner owner = new Owner();
            // Add pets to the owner - these shouldn't be affected by the mapping
            Pet pet = new Pet();
            pet.setName("Fluffy");
            owner.addPet(pet);

            OwnerRequest request = new OwnerRequest(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_ADDRESS, TEST_CITY, TEST_TELEPHONE);

            // Act
            Owner result = mapper.map(owner, request);

            // Assert
            assertEquals(1, result.getPets().size(), "Pets collection should be preserved");
            assertEquals("Fluffy", result.getPets().get(0).getName(), "Pet name should be preserved");
        }
    }
}
