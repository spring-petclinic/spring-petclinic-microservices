package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PetType Entity Tests")
class PetTypeTest {

    private static final int PET_TYPE_ID = 1;
    private static final String PET_TYPE_NAME = "cat";

    private PetType petType;

    @BeforeEach
    void setup() {
        // Arrange
        petType = new PetType();
        petType.setId(PET_TYPE_ID);
        petType.setName(PET_TYPE_NAME);
    }

    @Nested
    @DisplayName("Basic Property Tests")
    class BasicPropertyTests {

        @Test
        @DisplayName("Should get and set id correctly")
        void shouldGetAndSetIdCorrectly() {
            // Arrange
            final int newId = 5;

            // Act
            petType.setId(newId);

            // Assert
            assertThat(petType.getId()).isEqualTo(newId);
        }

        @Test
        @DisplayName("Should get and set name correctly")
        void shouldGetAndSetNameCorrectly() {
            // Arrange
            final String newName = "dog";

            // Act
            petType.setName(newName);

            // Assert
            assertThat(petType.getName()).isEqualTo(newName);
        }
    }

    @Nested
    @DisplayName("Object Method Tests")
    class ObjectMethodTests {

        @Test
        @DisplayName("Default constructor should initialize empty object")
        void defaultConstructorShouldInitializeEmptyObject() {
            // Act
            PetType newPetType = new PetType();

            // Assert
            assertThat(newPetType.getId()).isNull();
            assertThat(newPetType.getName()).isNull();
        }

        @Test
        @DisplayName("PetType with same values should be functionally equivalent")
        void petTypeWithSameValuesShouldBeFunctionallyEquivalent() {
            // Arrange
            PetType anotherPetType = new PetType();
            anotherPetType.setId(PET_TYPE_ID);
            anotherPetType.setName(PET_TYPE_NAME);

            // Act & Assert - not testing equals directly since PetType doesn't override it
            assertThat(anotherPetType.getId()).isEqualTo(petType.getId());
            assertThat(anotherPetType.getName()).isEqualTo(petType.getName());
        }
    }
}
