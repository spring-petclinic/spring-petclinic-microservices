package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(PetResource.class)
class PetResourceTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private OwnerResource ownerResource;

    @MockBean
    private PetRepository petRepository;

    @MockBean
    private OwnerRepository ownerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPetTypes_shouldReturnListOfPetTypes() throws Exception {
        PetType dog = new PetType();
        dog.setName("Dog");

        PetType cat = new PetType();
        cat.setName("Cat");

        List<PetType> petTypes = List.of(dog, cat);
        Mockito.when(petRepository.findPetTypes()).thenReturn(petTypes);

        mockMvc.perform(get("/petTypes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void processCreationForm_shouldCreatePet() throws Exception {
        PetRequest request = new PetRequest(1, Date.valueOf(LocalDate.of(2020, 1, 1)), "Buddy", 2);
        Owner owner = new Owner();
        Mockito.when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        PetType petType = new PetType();
        Mockito.when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(petType));

        Pet savedPet = new Pet();
        savedPet.setId(1);
        Mockito.when(petRepository.save(any(Pet.class))).thenReturn(savedPet);

        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void processCreationForm_ownerNotFound_shouldReturn404() throws Exception {
        PetRequest request = new PetRequest(1, Date.valueOf(LocalDate.of(2020, 1, 1)), "Buddy", 2);
        Mockito.when(ownerRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void processUpdateForm_shouldUpdatePet() throws Exception {
        PetRequest request = new PetRequest(5, Date.valueOf(LocalDate.of(2019, 5, 1)), "Max", 3);
        Pet existingPet = new Pet();
        existingPet.setId(5);

        PetType petType = new PetType();
        petType.setName("Parrot");

        Mockito.when(petRepository.findById(5)).thenReturn(Optional.of(existingPet));
        Mockito.when(petRepository.findPetTypeById(3)).thenReturn(Optional.of(petType));
        Mockito.when(petRepository.save(any(Pet.class))).thenReturn(existingPet);

        mockMvc.perform(put("/owners/any/pets/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }

    @Test
    void processUpdateForm_petNotFound_shouldReturn404() throws Exception {
        PetRequest request = new PetRequest(10, Date.valueOf(LocalDate.of(2018, 1, 1)), "Ghost", 4);
        Mockito.when(petRepository.findById(10)).thenReturn(Optional.empty());

        mockMvc.perform(put("/owners/any/pets/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void testProcessCreationForm_Success() throws JsonProcessingException, Exception {
        // Mock input data
        PetRequest request = new PetRequest(1, Date.valueOf(LocalDate.of(2020, 1, 1)), "Buddy", 2);

        // Mock Owner
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");

        // Mock the behavior of OwnerResource to simulate creating an Owner with ID 1
        when(ownerResource.createOwner(any(OwnerRequest.class))).thenAnswer(invocation -> {
            OwnerRequest ownerRequest = invocation.getArgument(0);
            Owner createdOwner = new Owner();
            createdOwner.setFirstName(ownerRequest.firstName());
            createdOwner.setLastName(ownerRequest.lastName());
            createdOwner.setAddress(ownerRequest.address());
            createdOwner.setCity(ownerRequest.city());
            createdOwner.setTelephone(ownerRequest.telephone());

            // Use reflection to set the ID
            var field = Owner.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(createdOwner, 1);
            return createdOwner;
        });

        // Mock PetType
        PetType petType = new PetType();
        petType.setId(2);

        // Mock Pet
        Pet savedPet = new Pet();
        savedPet.setId(1);
        savedPet.setName("Buddy");
        // Mock repository behavior
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(petType));
        when(petRepository.save(any(Pet.class))).thenReturn(savedPet);

        // Perform the POST request
        mockMvc.perform(post("/owners/1/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    }


    @Test
    void findPet_petNotFound_shouldReturn404() throws Exception {
        Mockito.when(petRepository.findById(100)).thenReturn(Optional.empty());

        mockMvc.perform(get("/owners/any/pets/100"))
            .andExpect(status().isNotFound());
    }
}