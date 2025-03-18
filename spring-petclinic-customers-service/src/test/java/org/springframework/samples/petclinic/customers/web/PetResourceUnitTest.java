package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PetResourceUnitTest {

    private MockMvc mockMvc;

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PetResource petResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        mockMvc = MockMvcBuilders.standaloneSetup(petResource)
            .setMessageConverters(converter)
            .setControllerAdvice(new RestResponseEntityExceptionHandler())
            .setValidator(new LocalValidatorFactoryBean())
            .build();
    }

    @Test
    void shouldGetPetTypes() throws Exception {
        // Arrange
        List<PetType> petTypes = Arrays.asList(
            createPetType(1, "cat"),
            createPetType(2, "dog"),
            createPetType(3, "lizard")
        );
        
        // Assuming PetResource has a way to get pet types
        // This might need adjustment based on your actual implementation
        when(petRepository.findPetTypes()).thenReturn(petTypes);

        // Act & Assert
        mockMvc.perform(get("/petTypes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("cat"))
                .andExpect(jsonPath("$[1].name").value("dog"))
                .andExpect(jsonPath("$[2].name").value("lizard"));
    }

    @Test
    void shouldGetPetById() throws Exception {
        // Arrange
        Pet pet = createPet(1, "Buddy", createPetType(1, "dog"));
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");  // Must set first name
        owner.setLastName("Doe");    // Must set last name
        pet.setOwner(owner);         // Must set owner on pet
        
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));

        // Act & Assert
        mockMvc.perform(get("/owners/*/pets/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.type.name").value("dog"));
    }

    @Test
    void shouldCreateNewPet() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(createPetType(2, "cat")));
        
        // Act & Assert
        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Leo\", \"birthDate\": \"2020-09-07\", \"typeId\": 2}"))
                .andExpect(status().isCreated());
        
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldUpdateExistingPet() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        
        Pet pet = createPet(1, "Buddy", createPetType(1, "dog"));
        pet.setOwner(owner);
        
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));
        // Add this line to mock finding the pet type
        when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(createPetType(2, "cat")));
        
        // Act & Assert - use the correct URL pattern as in the controller
        mockMvc.perform(put("/owners/*/pets/{petId}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 1, \"name\": \"Leo\", \"birthDate\": \"2020-09-07\", \"typeId\": 2}"))
                .andExpect(status().isNoContent());
        
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldReturnNotFoundForNonExistingPet() throws Exception {
        // Arrange
        when(petRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/owners/*/pets/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForNonExistingOwnerWhenCreatingPet() throws Exception {
        // Arrange
        when(ownerRepository.findById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/owners/999/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Leo\", \"birthDate\": \"2020-09-07\", \"typeId\": 2}"))
                .andExpect(status().isNotFound());
        
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidPetData() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"birthDate\": \"invalid-date\", \"type\": {}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForNonExistingPetType() throws Exception {
        // Arrange
        Pet pet = createPet(1, "Buddy", createPetType(1, "dog"));
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));
        when(petRepository.findPetTypeById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(put("/owners/*/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\": 1, \"name\": \"Leo\", \"birthDate\": \"2020-09-07\", \"typeId\": 999}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPetDetails() throws Exception {
        // Arrange
        Pet pet = createPet(1, "Buddy", createPetType(1, "dog"));
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        pet.setOwner(owner);
        pet.setBirthDate(Date.valueOf("2020-01-01"));
        
        when(petRepository.findById(1)).thenReturn(Optional.of(pet));

        // Act & Assert
        mockMvc.perform(get("/owners/*/pets/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.birthDate").value("2020-01-01"))
                .andExpect(jsonPath("$.type.name").value("dog"))
                .andExpect(jsonPath("$.owner").value("John Doe"));
    }

    @Test
    void shouldValidatePetTypeWhenCreating() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(petRepository.findPetTypeById(999)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Leo\", \"birthDate\": \"2020-09-07\", \"typeId\": 999}"))
                .andExpect(status().isNotFound());
        
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void shouldValidatePetNameWhenCreating() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(petRepository.findPetTypeById(1)).thenReturn(Optional.of(createPetType(1, "dog")));
        
        // Act & Assert
        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"birthDate\": \"2020-09-07\", \"typeId\": 1}"))
                .andExpect(status().isBadRequest());
        
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void shouldValidateBirthDateWhenCreating() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        
        // Act & Assert
        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Leo\", \"birthDate\": \"invalid-date\", \"typeId\": 1}"))
                .andExpect(status().isBadRequest());
        
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void shouldAddPetToOwnerWhenCreating() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        PetType catType = createPetType(2, "cat");
        
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(petRepository.findPetTypeById(2)).thenReturn(Optional.of(catType));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet savedPet = invocation.getArgument(0);
            savedPet.setId(10);
            return savedPet;
        });
        
        // Act & Assert
        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Leo\", \"birthDate\": \"2020-09-07\", \"typeId\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Leo"))
                .andExpect(jsonPath("$.type.name").value("cat"));
        
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldValidateOwnerIdWhenCreating() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/owners/0/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Leo\", \"birthDate\": \"2020-09-07\", \"typeId\": 1}"))
                .andExpect(status().isBadRequest());
        
        verify(petRepository, never()).save(any(Pet.class));
    }

    private PetType createPetType(int id, String name) {
        PetType petType = new PetType();
        petType.setId(id);
        petType.setName(name);
        return petType;
    }
    
    private Pet createPet(int id, String name, PetType petType) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(name);
        pet.setType(petType);
        pet.setBirthDate(Date.valueOf(LocalDate.now().minusYears(1)));
        return pet;
    }
}