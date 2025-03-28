package org.springframework.samples.petclinic.customers.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PetResourceTests {

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PetResource petResource;

    private Pet pet;
    private Owner owner;
    private PetRequest petRequest;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        pet = new Pet();
        pet.setId(1);
        pet.setName("Bobby");
        pet.setOwner(owner);

        petRequest = new PetRequest(1, new Date(), "Bobby", 1);

    }

    @Test
    void testGetPetTypes() {
        // Giả lập danh sách PetType
        PetType type = new PetType();
        type.setId(1);
        type.setName("Dog");

        when(petRepository.findPetTypes()).thenReturn(List.of(type));

        List<PetType> petTypes = petResource.getPetTypes();

        assertEquals(1, petTypes.size());
        assertEquals("Dog", petTypes.get(0).getName());
    }

    @Test
    void testProcessCreationForm() {
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        Pet createdPet = petResource.processCreationForm(petRequest, 1);

        assertNotNull(createdPet);
        assertEquals("Bobby", createdPet.getName());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void testProcessUpdateForm() {
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        petResource.processUpdateForm(petRequest);

        assertEquals("Bobby", pet.getName());
        verify(petRepository, times(1)).save(pet);
    }

    @Test
    void testFindPet() {
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));

        PetDetails petDetails = petResource.findPet(1);

        assertNotNull(petDetails);
        assertEquals("Bobby", petDetails.name());
    }
}