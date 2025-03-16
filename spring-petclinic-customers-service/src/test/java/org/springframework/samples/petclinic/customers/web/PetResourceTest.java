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


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void shouldReturnNotFoundWhenPetDoesNotExist() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty()); // ✅ Mock empty result

        mvc.perform(get("/owners/2/pets/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound()); // ✅ Expect 404 Not Found
    }

    @Test
    void shouldReturnPetWithCorrectJsonFormat() throws Exception {
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
    void shouldReturnNotFoundWhenUpdatingNonExistingPet() throws Exception {
        String updatedPetJson = """
        {
            "name": "Updated Max",
            "type": { "id": 5 }
        }
    """;

        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(put("/owners/2/pets/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedPetJson))
            .andExpect(status().isNotFound()); // ✅ Expect 404 Not Found
    }

    @Test
    void shouldCreatePetSuccessfully() throws Exception {
        Owner owner = new Owner();
        given(ownerRepository.findById(2)).willReturn(Optional.of(owner)); // ✅ Ensure the owner exists

        String validPetJson = """
        {
            "name": "Max",
            "type": { "id": 4 }
        }
    """; // ✅ Valid pet JSON

        mvc.perform(post("/owners/2/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPetJson))
            .andExpect(status().isCreated()); // ✅ Expect 201 Created
    }

    @Test
    void shouldReturnMethodNotAllowedWhenDeletingPet() throws Exception {
        mvc.perform(delete("/owners/2/pets/999"))
            .andExpect(status().isMethodNotAllowed()); // ✅ Expect 405
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
