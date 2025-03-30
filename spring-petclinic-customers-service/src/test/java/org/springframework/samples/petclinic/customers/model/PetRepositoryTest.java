package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Pet Repository Integration Tests")
class PetRepositoryTest {

    private static final int PET_ID_VALID = 1;
    private static final int PET_ID_INVALID = 999;
    private static final int PET_TYPE_ID_VALID = 1;
    private static final int PET_TYPE_ID_INVALID = 999;
    private static final String PET_NAME = "TestPet";
    private static final int OWNER_ID_VALID = 1;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Nested
    @DisplayName("Find Pet Operations")
    class FindPetOperations {

        @Test
        @DisplayName("should find pet by ID when exists")
        void shouldFindPetById() {
            // Arrange - Pet with ID 1 is pre-loaded via data.sql

            // Act
            Optional<Pet> result = petRepository.findById(PET_ID_VALID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(PET_ID_VALID);
        }

        @Test
        @DisplayName("should return empty when pet ID doesn't exist")
        void shouldReturnEmptyWhenPetNotFound() {
            // Act
            Optional<Pet> result = petRepository.findById(PET_ID_INVALID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Pet Type Operations")
    class PetTypeOperations {

        @Test
        @DisplayName("should find all pet types")
        void shouldFindAllPetTypes() {
            // Act
            List<PetType> petTypes = petRepository.findPetTypes();

            // Assert
            assertThat(petTypes).isNotEmpty();
            // We know there are 6 pet types in the test data
            assertThat(petTypes).hasSize(6);
        }

        @Test
        @DisplayName("should find pet type by ID when exists")
        void shouldFindPetTypeById() {
            // Act
            Optional<PetType> result = petRepository.findPetTypeById(PET_TYPE_ID_VALID);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(PET_TYPE_ID_VALID);
        }

        @Test
        @DisplayName("should return empty when pet type ID doesn't exist")
        void shouldReturnEmptyWhenPetTypeNotFound() {
            // Act
            Optional<PetType> result = petRepository.findPetTypeById(PET_TYPE_ID_INVALID);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Save Pet Operations")
    class SavePetOperations {

        @Test
        @DisplayName("should save a new pet")
        void shouldSaveNewPet() {
            // Arrange
            Owner owner = ownerRepository.findById(OWNER_ID_VALID).orElseThrow();

            Pet pet = new Pet();
            pet.setName(PET_NAME);
            pet.setBirthDate(new Date());

            PetType petType = petRepository.findPetTypeById(PET_TYPE_ID_VALID).orElseThrow();
            pet.setType(petType);
            pet.setOwner(owner);

            // Act
            Pet savedPet = petRepository.save(pet);

            // Assert
            assertThat(savedPet.getId()).isNotNull();
            assertThat(savedPet.getName()).isEqualTo(PET_NAME);
            assertThat(savedPet.getType().getId()).isEqualTo(PET_TYPE_ID_VALID);
            assertThat(savedPet.getOwner().getId()).isEqualTo(OWNER_ID_VALID);
        }

        @Test
        @DisplayName("should update an existing pet")
        void shouldUpdateExistingPet() {
            // Arrange
            Pet existingPet = petRepository.findById(PET_ID_VALID).orElseThrow();
            String updatedName = "UpdatedPetName";
            existingPet.setName(updatedName);

            // Act
            Pet updatedPet = petRepository.save(existingPet);

            // Assert
            assertThat(updatedPet.getId()).isEqualTo(PET_ID_VALID);
            assertThat(updatedPet.getName()).isEqualTo(updatedName);

            // Verify the change is persistent
            Pet reloadedPet = petRepository.findById(PET_ID_VALID).orElseThrow();
            assertThat(reloadedPet.getName()).isEqualTo(updatedName);
        }
    }

    @Nested
    @DisplayName("Pet-Owner Relationship")
    class PetOwnerRelationship {

        @Test
        @DisplayName("should fetch pet's owner")
        void shouldFetchPetOwner() {
            // Arrange - Pet with ID 1 belongs to Owner with ID 1 in test data

            // Act
            Pet pet = petRepository.findById(PET_ID_VALID).orElseThrow();
            Owner owner = pet.getOwner();

            // Assert
            assertThat(owner).isNotNull();
            assertThat(owner.getId()).isEqualTo(OWNER_ID_VALID);
        }
    }

    @Nested
    @DisplayName("Pet Type Sorting")
    class PetTypeSorting {

        @Test
        @DisplayName("should return pet types sorted by name")
        void shouldReturnPetTypesSortedByName() {
            // Act
            List<PetType> petTypes = petRepository.findPetTypes();

            // Assert
            assertThat(petTypes).isNotEmpty();

            // Verify the sorting order (alphabetical by name)
            for (int i = 0; i < petTypes.size() - 1; i++) {
                String currentName = petTypes.get(i).getName();
                String nextName = petTypes.get(i + 1).getName();
                assertThat(currentName.compareTo(nextName)).isLessThanOrEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("Pet-PetType Relationship")
    class PetPetTypeRelationship {

        @Test
        @DisplayName("should fetch pet's type")
        void shouldFetchPetType() {
            // Act
            Pet pet = petRepository.findById(PET_ID_VALID).orElseThrow();
            PetType petType = pet.getType();

            // Assert
            assertThat(petType).isNotNull();
            assertThat(petType.getId()).isNotNull();
            assertThat(petType.getName()).isNotEmpty();
        }

        @Test
        @DisplayName("should change pet's type")
        void shouldChangePetType() {
            // Arrange
            Pet pet = petRepository.findById(PET_ID_VALID).orElseThrow();
            int initialTypeId = pet.getType().getId();

            // Find a different pet type than the current one
            int newTypeId = initialTypeId == 1 ? 2 : 1;
            PetType newPetType = petRepository.findPetTypeById(newTypeId).orElseThrow();

            // Act
            pet.setType(newPetType);
            petRepository.save(pet);

            // Assert
            Pet reloadedPet = petRepository.findById(PET_ID_VALID).orElseThrow();
            assertThat(reloadedPet.getType().getId()).isEqualTo(newTypeId);
            assertThat(reloadedPet.getType().getId()).isNotEqualTo(initialTypeId);
        }
    }
}
