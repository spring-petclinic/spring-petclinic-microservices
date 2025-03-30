package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pet Entity Tests")
class PetTest {

    private static final int PET_ID = 1;
    private static final String PET_NAME = "Fluffy";
    private static final int PET_TYPE_ID = 1;
    private static final String PET_TYPE_NAME = "cat";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String BIRTH_DATE_STRING = "2020-03-09";

    private Pet pet;
    private Date birthDate;
    private PetType petType;
    private Owner owner;

    @BeforeEach
    void setup() throws ParseException {
        // Arrange common test objects
        birthDate = new SimpleDateFormat(DATE_FORMAT).parse(BIRTH_DATE_STRING);

        petType = new PetType();
        petType.setId(PET_TYPE_ID);
        petType.setName(PET_TYPE_NAME);

        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Anytown");
        owner.setTelephone("1234567890");

        pet = new Pet();
        pet.setId(PET_ID);
        pet.setName(PET_NAME);
        pet.setBirthDate(birthDate);
        pet.setType(petType);
        pet.setOwner(owner);
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
            pet.setId(newId);

            // Assert
            assertThat(pet.getId()).isEqualTo(newId);
        }

        @Test
        @DisplayName("Should get and set name correctly")
        void shouldGetAndSetNameCorrectly() {
            // Arrange
            final String newName = "Whiskers";

            // Act
            pet.setName(newName);

            // Assert
            assertThat(pet.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("Should get and set birth date correctly")
        void shouldGetAndSetBirthDateCorrectly() throws ParseException {
            // Arrange
            final Date newBirthDate = new SimpleDateFormat(DATE_FORMAT).parse("2019-01-01");

            // Act
            pet.setBirthDate(newBirthDate);

            // Assert
            assertThat(pet.getBirthDate()).isEqualTo(newBirthDate);
        }

        @Test
        @DisplayName("Should get and set type correctly")
        void shouldGetAndSetTypeCorrectly() {
            // Arrange
            PetType newType = new PetType();
            newType.setId(2);
            newType.setName("dog");

            // Act
            pet.setType(newType);

            // Assert
            assertThat(pet.getType()).isEqualTo(newType);
            assertThat(pet.getType().getName()).isEqualTo("dog");
        }

        @Test
        @DisplayName("Should get and set owner correctly")
        void shouldGetAndSetOwnerCorrectly() {
            // Arrange
            Owner newOwner = new Owner();
            newOwner.setFirstName("Jane");
            newOwner.setLastName("Smith");

            // Act
            pet.setOwner(newOwner);

            // Assert
            assertThat(pet.getOwner()).isEqualTo(newOwner);
            assertThat(pet.getOwner().getFirstName()).isEqualTo("Jane");
        }
    }

    @Nested
    @DisplayName("Object Method Tests")
    class ObjectMethodTests {

        @Test
        @DisplayName("toString should contain pet details")
        void toStringShouldContainPetDetails() {
            // Act
            String result = pet.toString();

            // Assert
            assertThat(result).contains(
                String.valueOf(PET_ID),
                PET_NAME,
                PET_TYPE_NAME,
                owner.getFirstName(),
                owner.getLastName()
            );
        }

        @Test
        @DisplayName("equals should return true for same pet")
        void equalsShouldReturnTrueForSamePet() {
            // Arrange
            Pet samePet = new Pet();
            samePet.setId(PET_ID);
            samePet.setName(PET_NAME);
            samePet.setBirthDate(birthDate);
            samePet.setType(petType);
            samePet.setOwner(owner);

            // Act & Assert
            assertThat(pet.equals(samePet)).isTrue();
            assertThat(pet.hashCode()).isEqualTo(samePet.hashCode());
        }

        @Test
        @DisplayName("equals should return false for different pet")
        void equalsShouldReturnFalseForDifferentPet() {
            // Arrange
            Pet differentPet = new Pet();
            differentPet.setId(2);
            differentPet.setName("Different");
            differentPet.setBirthDate(birthDate);
            differentPet.setType(petType);
            differentPet.setOwner(owner);

            // Act & Assert
            assertThat(pet.equals(differentPet)).isFalse();
        }

        @Test
        @DisplayName("equals should return false for null")
        void equalsShouldReturnFalseForNull() {
            // Act & Assert
            assertThat(pet.equals(null)).isFalse();
        }

        @Test
        @DisplayName("equals should return false for different object type")
        void equalsShouldReturnFalseForDifferentObjectType() {
            // Act & Assert
            assertThat(pet.equals("not a pet")).isFalse();
        }
    }
}
