package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.sql.Date;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ControllerAdvice
class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
}

public class OwnerResourceUnitTest {

    private MockMvc mockMvc;

    @Mock
    private OwnerRepository ownerRepository;
    // test
    @Mock
    private OwnerEntityMapper ownerEntityMapper;

    @InjectMocks
    private OwnerResource ownerResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ownerResource)
            .setControllerAdvice(new RestResponseEntityExceptionHandler())
            .build();

        // Setup default behavior for mapper
        Owner defaultOwner = new Owner();
        when(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class))).thenAnswer(invocation -> {
            Owner owner = invocation.getArgument(0);
            OwnerRequest request = invocation.getArgument(1);
            owner.setFirstName(request.firstName());
            owner.setLastName(request.lastName());
            owner.setAddress(request.address());
            owner.setCity(request.city());
            owner.setTelephone(request.telephone());
            return owner;
        });
    }

    @Test
    void shouldGetOwnerById() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        // Act & Assert
        mockMvc.perform(get("/owners/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void shouldReturnNotFoundForNonExistingOwner() throws Exception {
        // Arrange
        when(ownerRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/owners/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewOwner() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("New York");
        owner.setTelephone("1234567890");
        
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        // Act & Assert
        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"address\": \"123 Main St\", \"city\": \"New York\", \"telephone\": \"1234567890\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateExistingOwner() throws Exception {
        // Arrange
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("New York");
        owner.setTelephone("1234567890");
        
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        // Act & Assert
        mockMvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"Johnny\", \"lastName\": \"Doe\", \"address\": \"123 Main St\", \"city\": \"Chicago\", \"telephone\": \"1234567890\"}"))
                .andExpect(status().isNoContent());
        
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void shouldGetAllOwners() throws Exception {
        // Arrange
        List<Owner> owners = Arrays.asList(
            createOwner(1, "John", "Doe"),
            createOwner(2, "Jane", "Smith")
        );
        when(ownerRepository.findAll()).thenReturn(owners);

        // Act & Assert
        mockMvc.perform(get("/owners")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void shouldReturnBadRequestForInvalidOwnerData() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"\", \"lastName\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSearchOwnersByLastName() throws Exception {
        // Arrange
        List<Owner> owners = Arrays.asList(
            createOwner(1, "John", "Smith"),
            createOwner(2, "Jane", "Smith")
        );
        when(ownerRepository.findByLastName("Smith")).thenReturn(owners);

        // Act & Assert
        mockMvc.perform(get("/owners/search?lastName=Smith")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Smith"))
                .andExpect(jsonPath("$[1].lastName").value("Smith"));
    }

    @Test
    void shouldReturnNotFoundForUpdateNonExistingOwner() throws Exception {
        // Arrange
        when(ownerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/owners/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"address\": \"123 Main St\", \"city\": \"Chicago\", \"telephone\": \"1234567890\"}"))
                .andExpect(status().isNotFound());
        
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoOwnersFound() throws Exception {
        // Arrange
        when(ownerRepository.findAll()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/owners")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldValidateOwnerFieldsWhenCreating() throws Exception {
        // Test missing telephone
        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"address\": \"123 Main St\", \"city\": \"New York\"}"))
                .andExpect(status().isBadRequest());

        // Test invalid telephone format
        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"address\": \"123 Main St\", \"city\": \"New York\", \"telephone\": \"abc\"}"))
                .andExpect(status().isBadRequest());

        // Test missing address
        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"city\": \"New York\", \"telephone\": \"1234567890\"}"))
                .andExpect(status().isBadRequest());

        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void shouldValidateOwnerFieldsWhenUpdating() throws Exception {
        Owner existingOwner = new Owner();
        existingOwner.setId(1);
        when(ownerRepository.findById(1)).thenReturn(Optional.of(existingOwner));

        // Test missing telephone
        mockMvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"address\": \"123 Main St\", \"city\": \"New York\"}"))
                .andExpect(status().isBadRequest());

        // Test invalid telephone format
        mockMvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"John\", \"lastName\": \"Doe\", \"address\": \"123 Main St\", \"city\": \"New York\", \"telephone\": \"abc\"}"))
                .andExpect(status().isBadRequest());

        verify(ownerRepository, never()).save(any(Owner.class));
    }

    @Test
    void shouldReturnNotFoundWhenSearchingNonExistingLastName() throws Exception {
        // Arrange
        when(ownerRepository.findByLastName("Unknown")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/owners/search?lastName=Unknown")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenSearchingWithEmptyLastName() throws Exception {
        // Arrange
        when(ownerRepository.findByLastName("")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/owners/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOwnerWithPets() throws Exception {
        // Arrange
        Owner owner = createOwnerWithPets();
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        // Act & Assert
        mockMvc.perform(get("/owners/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.pets[0].name").value("Max"))
                .andExpect(jsonPath("$.pets[0].type.name").value("dog"));
    }
    
    private Owner createOwner(int id, String firstName, String lastName) {
        Owner owner = new Owner();
        owner.setId(id);
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        return owner;
    }

    private Owner createOwnerWithPets() {
        Owner owner = createOwner(1, "John", "Doe");
        owner.setAddress("123 Main St");
        owner.setCity("New York");
        owner.setTelephone("1234567890");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Max");
        PetType dogType = new PetType();
        dogType.setId(1);
        dogType.setName("dog");
        pet.setType(dogType);
        pet.setBirthDate(Date.valueOf(LocalDate.now().minusYears(2)));
        owner.addPet(pet);

        return owner;
    }
}