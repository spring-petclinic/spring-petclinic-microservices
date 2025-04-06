package org.springframework.samples.petclinic.customers.web;

import java.util.Optional;

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


import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    // -------------------------------------------------------------------------------------------
    @Test
    void shouldGetAListOfPetTypes() throws Exception {
        PetType petType = new PetType();
        petType.setId(1);
        petType.setName("dog");
        given(petRepository.findPetTypes()).willReturn(asList(petType));

        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("dog"));
    }   
    
    @Test
    void processCreationForm_validOwnerId_shouldReturnCreated() throws Exception {
        Pet pet = setupPet();
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        given(ownerRepository.findById(2))
            .willReturn(Optional.of(owner));
        given(petRepository.findPetTypeById(6))
            .willReturn(Optional.of(new PetType()));

        mvc.perform(post("/owners/2/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Basil\",\"birthDate\":\"2023-10-01\",\"typeId\":6}"))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnNotFoundWhenOwnerIdDoesNotExist() throws Exception {
        int nonExistentOwnerId = 999;
        given(ownerRepository.findById(nonExistentOwnerId)).willReturn(java.util.Optional.empty());

        mvc.perform(post("/owners/{ownerId}/pets", nonExistentOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Buddy\",\"birthDate\":\"2023-01-01\",\"typeId\":1}")
        )
        .andExpect(status().isNotFound());
    }
    // -------------------------------------------------------------------------------------------

    private Pet setupPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();

        pet.setId(2);
        pet.setName("Basil");
        pet.setBirthDate(java.sql.Date.valueOf("2023-10-01"));

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }
}
