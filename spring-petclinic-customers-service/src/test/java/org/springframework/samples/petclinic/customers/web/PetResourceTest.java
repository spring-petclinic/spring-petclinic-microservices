package org.springframework.samples.petclinic.customers.web;

import java.time.ZoneId;
import java.util.Arrays;
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


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.hamcrest.Matchers.hasSize;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.any;

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

    @Autowired
    ObjectMapper objectMapper;

    // [TEST] [CORRECT WAY]: GET A PET
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

    // [GET] [CAN'T FIND PET]: GET A PET
    @Test
    void shouldReturn404WhenPetNotFound() throws Exception {
        given(petRepository.findById(2)).willReturn(Optional.empty());

        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())  // 404 Not Found
            .andExpect(jsonPath("$.message").value("Pet 2 not found"));
    }

    // [TEST] [CORRECT WAY]: GET PET'S TYPES
    @Test
    void shouldGetPetTypes() throws Exception {
        PetType type1 = new PetType();
        type1.setId(1);
        type1.setName("Dog");

        PetType type2 = new PetType();
        type2.setId(2);
        type2.setName("Cat");

        given(petRepository.findPetTypes()).willReturn(Arrays.asList(type1, type2));

        mvc.perform(get("/petTypes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Dog"))
            .andExpect(jsonPath("$[1].name").value("Cat"));
    }

    // [TEST] [CORRECT WAY]: CREATE A PET
    @Test
    void shouldCreatePet() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);

        PetType type = new PetType();
        type.setId(6);
        type.setName("Dog");

        Pet pet = new Pet();
        pet.setId(2);
        pet.setName("Basil");
        pet.setType(type);
        pet.setBirthDate(Date.from(
            LocalDate.of(2019, 12, 24)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        ));
        owner.addPet(pet);

        Date birthDate = Date.from(
            LocalDate.of(2019, 12, 24)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        );

        PetRequest request = new PetRequest(2, birthDate, "Basil", 6);

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(type));
        given(petRepository.save(any(Pet.class))).willReturn(pet);

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    // [TEST] [CAN'T FIND OWNER]: CREATE A PET
    @Test
    void shouldReturn404WhenOwnerNotFound() throws Exception {
        PetRequest request = new PetRequest(2, new Date(), "Basil", 6);

        given(ownerRepository.findById(1)).willReturn(Optional.empty());

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())  // 404 Not Found
            .andExpect(jsonPath("$.message").value("Owner 1 not found"));
    }

    // [TEST] [LACK OF REQUEST]: CREATE A PET
    // TODO: Add check vaid data in PetRequest.java and add handle return status code in PetResource.java
//    @Test
//    void shouldReturn400WhenPetRequestIsInvalid() throws Exception {
//        PetRequest invalidRequest = new PetRequest(0, new Date(), "", 0);  // Tên trống và typeId không hợp lệ
//
//        mvc.perform(post("/owners/1/pets")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(invalidRequest)))
//            .andExpect(status().isBadRequest())  // 400 Bad Request
//            .andExpect(jsonPath("$.message").value("Validation failed for pet request"));
//    }

    // [TEST] [CORRECT WAY]: UPDATE A PET
    @Test
    void shouldUpdatePet() throws Exception {
        Pet pet = setupPet();

        Date birthDate = Date.from(
            LocalDate.of(2019, 12, 24)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        );
        PetRequest request = new PetRequest(2, birthDate, "UpdatedName", 6);

        given(petRepository.findById(2)).willReturn(Optional.of(pet));
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(pet.getType()));
        given(petRepository.save(any(Pet.class))).willReturn(pet);

        mvc.perform(put("/owners/99/pets/2") // path ownerId is wildcarded in controller
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(petRepository).save(any(Pet.class));
    }

    // [TEST] [UPDATE WITH INCORRECT PET ID]: UPDATE A PET
    @Test
    void shouldReturn404WhenPetToUpdateNotFound() throws Exception {
        // id 99 không tồn tại
        PetRequest request = new PetRequest(99, new Date(), "Basil", 6);

        given(petRepository.findById(99)).willReturn(Optional.empty());

        mvc.perform(put("/owners/*/pets/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())  // 404 Not Found
            .andExpect(jsonPath("$.message").value("Pet 99 not found"));
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
