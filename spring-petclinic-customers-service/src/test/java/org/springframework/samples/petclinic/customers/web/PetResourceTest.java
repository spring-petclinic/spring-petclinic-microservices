package org.springframework.samples.petclinic.customers.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
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


import static org.mockito.ArgumentMatchers.any;
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

    //test for post method
    @Test
    void createPet_whenOwnerDoesNotExist_returnNotFound() throws Exception {
        int ownerId = 99;

        given(ownerRepository.findById(ownerId)).willReturn(Optional.empty());

        PetRequest petRequest = setupPetRequest();

        String json = new ObjectMapper().writeValueAsString(petRequest);

        mvc.perform(post("/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNotFound());
    }
    @Test
    void createPet_whenOwnerExist_returnCreatedPet() throws Exception {

        int ownerId = 2;
        Owner owner = setupOwner();
        PetRequest petRequest = setupPetRequest();
        PetType type = setupPetType();
        Pet savePet = setupPet();

        given(ownerRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(petRepository.findPetTypeById(petRequest.typeId())).willReturn(Optional.of(type));
        given(petRepository.findById(2)).willReturn(Optional.of(savePet));
        given(petRepository.save(any(Pet.class))).willReturn(savePet);


        String json = new ObjectMapper().writeValueAsString(petRequest);

        mvc.perform(post("/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(2));
    }
    @Test
    void createPet_whenPetRequestIsNull_returnBadRequest() throws Exception {
        int ownerId = 2;

        given(ownerRepository.findById(ownerId)).willReturn(Optional.of(setupOwner()));

        mvc.perform(post("/owners/{ownerId}/pets", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
            .andExpect(status().isBadRequest());
    }


    private Pet setupPet() {
        Owner owner = setupOwner();

        Pet pet = new Pet();

        pet.setName("Basil");
        pet.setId(2);

        PetType petType = setupPetType();
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }

    private Owner setupOwner() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");
        return owner;
    }

    private PetRequest setupPetRequest() throws ParseException {
        String dateString = "2025/04/10";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date birthDate = formatter.parse(dateString);
        return new PetRequest(2, birthDate, "Basil", 2);
    }

    private PetType setupPetType() {
        PetType petType = new PetType();
        petType.setId(2);
        petType.setName("dog");
        return petType;
    }
}
