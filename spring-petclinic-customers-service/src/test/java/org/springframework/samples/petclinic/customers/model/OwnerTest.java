package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Owner Entity Tests")
class OwnerTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String ADDRESS = "123 Main St";
    private static final String CITY = "Anytown";
    private static final String TELEPHONE = "1234567890";

    private Owner owner;

    @BeforeEach
    void setup() {
        // Arrange a standard owner for each test
        owner = new Owner();
        owner.setFirstName(FIRST_NAME);
        owner.setLastName(LAST_NAME);
        owner.setAddress(ADDRESS);
        owner.setCity(CITY);
        owner.setTelephone(TELEPHONE);
    }

    @Nested
    @DisplayName("Basic Property Tests")
    class BasicPropertyTests {

        @Test
        @DisplayName("Should get and set first name correctly")
        void shouldGetAndSetFirstNameCorrectly() {
            // Arrange
            final String newFirstName = "Jane";

            // Act
            owner.setFirstName(newFirstName);

            // Assert
            assertThat(owner.getFirstName()).isEqualTo(newFirstName);
        }

        @Test
        @DisplayName("Should get and set last name correctly")
        void shouldGetAndSetLastNameCorrectly() {
            // Arrange
            final String newLastName = "Smith";

            // Act
            owner.setLastName(newLastName);

            // Assert
            assertThat(owner.getLastName()).isEqualTo(newLastName);
        }

        @Test
        @DisplayName("Should get and set address correctly")
        void shouldGetAndSetAddressCorrectly() {
            // Arrange
            final String newAddress = "456 Oak St";

            // Act
            owner.setAddress(newAddress);

            // Assert
            assertThat(owner.getAddress()).isEqualTo(newAddress);
        }

        @Test
        @DisplayName("Should get and set city correctly")
        void shouldGetAndSetCityCorrectly() {
            // Arrange
            final String newCity = "Othertown";

            // Act
            owner.setCity(newCity);

            // Assert
            assertThat(owner.getCity()).isEqualTo(newCity);
        }

        @Test
        @DisplayName("Should get and set telephone correctly")
        void shouldGetAndSetTelephoneCorrectly() {
            // Arrange
            final String newTelephone = "9876543210";

            // Act
            owner.setTelephone(newTelephone);

            // Assert
            assertThat(owner.getTelephone()).isEqualTo(newTelephone);
        }
    }

    @Nested
    @DisplayName("Pet Management Tests")
    class PetManagementTests {

        @Test
        @DisplayName("Should return empty pets list for new owner")
        void shouldReturnEmptyPetsListForNewOwner() {
            // Act
            List<Pet> pets = owner.getPets();

            // Assert
            assertThat(pets).isNotNull();
            assertThat(pets).isEmpty();
        }

        @Test
        @DisplayName("Should add pet to owner correctly")
        void shouldAddPetToOwnerCorrectly() throws ParseException {
            // Arrange
            Pet pet = createPet("Fluffy", "cat");

            // Act
            owner.addPet(pet);

            // Assert
            assertThat(owner.getPets()).hasSize(1);
            assertThat(owner.getPets().get(0)).isEqualTo(pet);
            assertThat(pet.getOwner()).isEqualTo(owner);
        }

        @Test
        @DisplayName("Should return pets sorted by name")
        void shouldReturnPetsSortedByName() throws ParseException {
            // Arrange
            Pet pet1 = createPet("Whiskers", "cat");
            Pet pet2 = createPet("Buddy", "dog");
            Pet pet3 = createPet("Max", "hamster");

            // Act - Add pets in non-alphabetical order
            owner.addPet(pet1);  // Whiskers
            owner.addPet(pet2);  // Buddy
            owner.addPet(pet3);  // Max

            List<Pet> sortedPets = owner.getPets();

            // Assert - Pets should be returned in alphabetical order by name
            assertThat(sortedPets).hasSize(3);
            assertThat(sortedPets.get(0).getName()).isEqualTo("Buddy");
            assertThat(sortedPets.get(1).getName()).isEqualTo("Max");
            assertThat(sortedPets.get(2).getName()).isEqualTo("Whiskers");
        }

        @Test
        @DisplayName("Should return unmodifiable pets list")
        void shouldReturnUnmodifiablePetsList() throws ParseException {
            // Arrange
            Pet pet = createPet("Fluffy", "cat");
            owner.addPet(pet);

            // Act
            List<Pet> pets = owner.getPets();

            // Assert
            assertThat(pets).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Object Method Tests")
    class ObjectMethodTests {

        @Test
        @DisplayName("toString should contain owner details")
        void toStringShouldContainOwnerDetails() {
            // Act
            String result = owner.toString();

            // Assert
            assertThat(result).contains(
                FIRST_NAME,
                LAST_NAME,
                ADDRESS,
                CITY,
                TELEPHONE
            );
        }
    }

    /**
     * Helper method to create a pet with the given name and type
     */
    private Pet createPet(String name, String typeName) throws ParseException {
        Pet pet = new Pet();
        pet.setName(name);

        PetType petType = new PetType();
        petType.setName(typeName);
        pet.setType(petType);

        Date birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-01");
        pet.setBirthDate(birthDate);

        return pet;
    }
}
