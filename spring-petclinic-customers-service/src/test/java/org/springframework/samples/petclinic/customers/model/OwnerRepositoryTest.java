package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Owner Repository Integration Tests")
class OwnerRepositoryTest {

    private static final int OWNER_ID_VALID = 1;
    private static final int OWNER_ID_INVALID = 999;
    private static final String OWNER_FIRST_NAME = "John";
    private static final String OWNER_LAST_NAME = "Doe";
    private static final String OWNER_ADDRESS = "123 Main St";
    private static final String OWNER_CITY = "Anytown";
    private static final String OWNER_TELEPHONE = "1234567890";

    @Autowired
    private OwnerRepository ownerRepository;

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("should find owner by ID when exists")
        void shouldFindOwnerById() {
            // Arrange - Owner with ID 1 is pre-loaded via data.sql

            // Act
            Optional<Owner> result = ownerRepository.findById(OWNER_ID_VALID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(OWNER_ID_VALID);
        }

        @Test
        @DisplayName("should return empty when owner ID doesn't exist")
        void shouldReturnEmptyWhenOwnerNotFound() {
            // Act
            Optional<Owner> result = ownerRepository.findById(OWNER_ID_INVALID);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should find all owners")
        void shouldFindAllOwners() {
            // Act
            List<Owner> owners = ownerRepository.findAll();

            // Assert
            assertThat(owners).isNotEmpty();
            // We know there are 10 owners in the test data
            assertThat(owners).hasSizeGreaterThanOrEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("should save a new owner")
        void shouldSaveNewOwner() {
            // Arrange
            Owner owner = new Owner();
            owner.setFirstName(OWNER_FIRST_NAME);
            owner.setLastName(OWNER_LAST_NAME);
            owner.setAddress(OWNER_ADDRESS);
            owner.setCity(OWNER_CITY);
            owner.setTelephone(OWNER_TELEPHONE);

            // Act
            Owner savedOwner = ownerRepository.save(owner);

            // Assert
            assertThat(savedOwner.getId()).isNotNull();
            assertThat(savedOwner.getFirstName()).isEqualTo(OWNER_FIRST_NAME);
            assertThat(savedOwner.getLastName()).isEqualTo(OWNER_LAST_NAME);
            assertThat(savedOwner.getAddress()).isEqualTo(OWNER_ADDRESS);
            assertThat(savedOwner.getCity()).isEqualTo(OWNER_CITY);
            assertThat(savedOwner.getTelephone()).isEqualTo(OWNER_TELEPHONE);
        }

        @Test
        @DisplayName("should update an existing owner")
        void shouldUpdateExistingOwner() {
            // Arrange
            Owner existingOwner = ownerRepository.findById(OWNER_ID_VALID).orElseThrow();
            String updatedLastName = "UpdatedLastName";
            existingOwner.setLastName(updatedLastName);

            // Act
            Owner updatedOwner = ownerRepository.save(existingOwner);

            // Assert
            assertThat(updatedOwner.getId()).isEqualTo(OWNER_ID_VALID);
            assertThat(updatedOwner.getLastName()).isEqualTo(updatedLastName);

            // Verify the change is persistent
            Owner reloadedOwner = ownerRepository.findById(OWNER_ID_VALID).orElseThrow();
            assertThat(reloadedOwner.getLastName()).isEqualTo(updatedLastName);
        }
    }

    @Nested
    @DisplayName("Owner-Pet Relationship")
    class OwnerPetRelationship {

        @Test
        @DisplayName("should fetch owner's pets")
        void shouldFetchOwnerPets() {
            // Arrange - Owner with ID 6 has 2 pets in test data
            final int OWNER_WITH_MULTIPLE_PETS_ID = 6;

            // Act
            Owner owner = ownerRepository.findById(OWNER_WITH_MULTIPLE_PETS_ID).orElseThrow();
            List<Pet> pets = owner.getPets();

            // Assert
            assertThat(pets).isNotEmpty();
            assertThat(pets).hasSize(2);
            assertThat(pets.get(0).getOwner().getId()).isEqualTo(OWNER_WITH_MULTIPLE_PETS_ID);
            assertThat(pets.get(1).getOwner().getId()).isEqualTo(OWNER_WITH_MULTIPLE_PETS_ID);
        }

        @Test
        @DisplayName("should add a pet to owner")
        void shouldAddPetToOwner() {
            // Arrange
            Owner owner = ownerRepository.findById(OWNER_ID_VALID).orElseThrow();
            int initialPetCount = owner.getPets().size();

            Pet newPet = new Pet();
            newPet.setName("TestPet");
            newPet.setBirthDate(java.sql.Date.valueOf("2020-01-01"));

            PetType petType = new PetType();
            petType.setId(1); // Cat
            newPet.setType(petType);

            // Act
            owner.addPet(newPet);
            ownerRepository.save(owner);

            // Assert
            Owner reloadedOwner = ownerRepository.findById(OWNER_ID_VALID).orElseThrow();
            assertThat(reloadedOwner.getPets()).hasSize(initialPetCount + 1);

            // Verify the new pet is in the list and has the correct owner
            Pet addedPet = reloadedOwner.getPets().stream()
                .filter(p -> "TestPet".equals(p.getName()))
                .findFirst()
                .orElseThrow();

            assertThat(addedPet.getOwner()).isEqualTo(reloadedOwner);
        }
    }
}
