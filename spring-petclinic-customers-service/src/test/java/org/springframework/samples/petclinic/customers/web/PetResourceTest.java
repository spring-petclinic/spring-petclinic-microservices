package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetResourceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PetResource petResource;

    private Pet pet;
    private Owner owner;
    private PetRequest petRequest;
    private PetType petType;
    private Date birthDate;

    @BeforeEach
    void setUp() {
        // Setup owner
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        // Setup pet type
        petType = new PetType();
        petType.setId(2);
        petType.setName("Dog");

        // Setup birthdate
        birthDate = new Date();

        // Setup pet
        pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setOwner(owner);
        pet.setBirthDate(birthDate);
        pet.setType(petType);

        // Setup pet request
        petRequest = new PetRequest(
            1,
            birthDate,
            "Buddy",
            2
        );
    }

    @Test
    void testGetPetTypes() {
        // Arrange
        PetType cat = new PetType();
        cat.setId(1);
        cat.setName("Cat");

        List<PetType> petTypes = Arrays.asList(petType, cat);
        when(petRepository.findPetTypes()).thenReturn(petTypes);

        // Act
        List<PetType> result = petResource.getPetTypes();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Dog");
        assertThat(result.get(1).getName()).isEqualTo("Cat");
        verify(petRepository).findPetTypes();
    }

    @Test
    void testProcessCreationForm() {
        // Arrange
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(petType));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pet savedPet = petResource.processCreationForm(petRequest, 1);

        // Assert
        assertThat(savedPet).isNotNull();
        assertThat(savedPet.getName()).isEqualTo("Buddy");
        assertThat(savedPet.getBirthDate()).isEqualTo(birthDate);
        assertThat(savedPet.getType()).isEqualTo(petType);
        assertThat(savedPet.getOwner()).isEqualTo(owner);
        verify(ownerRepository).findById(1);
        verify(petRepository).findPetTypeById(2);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void testProcessCreationFormOwnerNotFound() {
        // Arrange
        when(ownerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            petResource.processCreationForm(petRequest, 999);
        });

        verify(ownerRepository).findById(999);
    }

    @Test
    void testProcessUpdateForm() {
        // Arrange
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));
        when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(petType));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        petResource.processUpdateForm(petRequest);

        // Assert
        verify(petRepository).findById(1);
        verify(petRepository).findPetTypeById(2);
        verify(petRepository).save(pet);
    }

    @Test
    void testProcessUpdateFormPetNotFound() {
        // Arrange
        PetRequest invalidPetRequest = new PetRequest(999, birthDate, "Ghost", 2);
        when(petRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            petResource.processUpdateForm(invalidPetRequest);
        });

        verify(petRepository).findById(999);
    }

    @Test
    void testFindPet() {
        // Arrange
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));

        // Act
        PetDetails petDetails = petResource.findPet(1);

        // Assert
        assertThat(petDetails).isNotNull();
        assertThat(petDetails.id()).isEqualTo(1);
        assertThat(petDetails.name()).isEqualTo("Buddy");
        assertThat(petDetails.owner()).isEqualTo("John Doe");
        assertThat(petDetails.birthDate()).isEqualTo(birthDate);
        assertThat(petDetails.type()).isEqualTo(petType);
        verify(petRepository).findById(1);
    }

    @Test
    void testFindPetNotFound() {
        // Arrange
        when(petRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            petResource.findPet(999);
        });

        verify(petRepository).findById(999);
    }

    @Test
    void testSaveMethod() {
        // Arrange
        Pet newPet = new Pet();
        PetRequest newPetRequest = new PetRequest(0, birthDate, "Max", 2);

        when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(petType));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet savedPet = invocation.getArgument(0);
            savedPet.setId(5); // Simulate ID assignment by database
            return savedPet;
        });

        // Act - using processCreationForm which calls the private save method
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        Pet savedPet = petResource.processCreationForm(newPetRequest, 1);

        // Assert
        assertThat(savedPet.getName()).isEqualTo("Max");
        assertThat(savedPet.getBirthDate()).isEqualTo(birthDate);
        assertThat(savedPet.getType()).isEqualTo(petType);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void testSaveMethodWithNoPetType() {
        // Arrange
        Pet newPet = new Pet();
        PetRequest newPetRequest = new PetRequest(0, birthDate, "Max", 999); // Invalid type ID

        when(petRepository.findPetTypeById(999)).thenReturn(Optional.empty());
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - using processCreationForm which calls the private save method
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        Pet savedPet = petResource.processCreationForm(newPetRequest, 1);

        // Assert
        assertThat(savedPet.getName()).isEqualTo("Max");
        assertThat(savedPet.getBirthDate()).isEqualTo(birthDate);
        assertThat(savedPet.getType()).isNull(); // Type should be null since we didn't find one
        verify(petRepository).findPetTypeById(999);
        verify(petRepository).save(any(Pet.class));
    }
}
