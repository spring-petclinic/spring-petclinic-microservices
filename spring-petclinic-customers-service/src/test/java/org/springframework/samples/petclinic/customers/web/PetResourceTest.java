package org.springframework.samples.petclinic.customers.web;

import java.lang.StackWalker.Option;
import java.util.Optional;
import java.util.List;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.samples.petclinic.customers.web.PetRequest;


import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Maciej Szarlinski
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PetRepository petRepository;

    @MockBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetAPetInJSonFormat() throws Exception {

        Pet pet = setupPet();

        given(petRepository.findById(2)).willReturn(Optional.of(pet));


        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    @Test
    void shouldFindPetById() throws Exception {
        // Given
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Max");
        owner.addPet(pet);

        given(petRepository.findById(1)).willReturn(Optional.of(pet));

        // When/Then
        mvc.perform(get("/owners/1/pets/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Max"));
    }

    @Test 
    void shouldReturnNotFoundForNonExistingPet() throws Exception {
        given(petRepository.findById(1)).willReturn(Optional.empty());

        mvc.perform(get("/owners/99/pets/99").accept(MediaType.APPLICATION_JSON)) 
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRetrievePetWithAllAttributes() throws Exception {
        Pet pet = setupPet();
        pet.setBirthDate(java.sql.Date.valueOf("2020-05-10"));

        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6))
            .andExpect(jsonPath("$.birthDate").value("2020-05-10"));
    }

    @Test 
    void shouldHandleNullPetType() throws Exception {
        Pet pet = setupPet();
        pet.setId(3);
        pet.setName("Shadow");
        pet.setType(null);  

        given(petRepository.findById(3)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/3").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Shadow"))
            .andExpect(jsonPath("$.type").doesNotExist());
    }

    @Test
    void shouldReturnPetWithCorshouldReturnPetWithCorrectJsonFormatrectJsonFormat() throws Exception {
        Pet pet = setupPet();

        given(petRepository.findById(2)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    @Test
    void shouldReturnNotFoundWhenPetDoesNotExist() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/2/pets/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnMethodNotAllowedWhenDeletingPet() throws Exception {
        mvc.perform(delete("/owners/2/pets/999"))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldGetAllPetTypes() throws Exception {
        // Arrange
        List<PetType> petTypes = List.of(
            createPetType(1, "Dog"),
            createPetType(2, "Cat"),
            createPetType(3, "Bird")
        );
        
        given(petRepository.findPetTypes()).willReturn(petTypes);
        
        // Act & Assert
        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Dog"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Cat"))
            .andExpect(jsonPath("$[2].id").value(3))
            .andExpect(jsonPath("$[2].name").value("Bird"));
    }

    @Test
    void shouldThrowExceptionWhenOwnerNotFoundDuringPetCreation() throws Exception {
        // Given
        given(ownerRepository.findById(999)).willReturn(Optional.empty());
        
        String newPetJson = """
        {
            "name": "Ghost",
            "birthDate": "2021-04-20",
            "typeId": 2
        }
        """;
        
        // When/Then
        mvc.perform(post("/owners/999/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPetJson))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateExistingPet() throws Exception {
        // Given
        Pet existingPet = setupPet();
        PetType catType = createPetType(1, "Cat");
        
        given(petRepository.findById(2)).willReturn(Optional.of(existingPet));
        given(petRepository.findPetTypeById(1)).willReturn(Optional.of(catType));
        
        String updatePetJson = """
        {
            "id": 2,
            "name": "BasilUpdated",
            "birthDate": "2019-08-15",
            "typeId": 1
        }
        """;
        
        // When/Then
        mvc.perform(put("/owners/2/pets/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePetJson))
            .andExpect(status().isNoContent());
        
        // Verify that pet was saved
        org.mockito.Mockito.verify(petRepository).save(existingPet);
    }

    // Helper method to create a PetType with id and name
    private PetType createPetType(int id, String name) {
        PetType petType = new PetType();
        petType.setId(id);
        petType.setName(name);
        return petType;
    }

    private Pet setupPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();

        pet.setName("Basil");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }
 }
