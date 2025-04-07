package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void shouldGetEmptyPetTypes() throws Exception {
        // Given
        given(petRepository.findPetTypes()).willReturn(Collections.emptyList());

        // When
        mvc.perform(get("/petTypes")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json"))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldGetPetTypes() throws Exception {
        PetType dog = new PetType();
        dog.setId(1);
        dog.setName("Chó");
        PetType cat = new PetType();
        cat.setId(2);
        cat.setName("Mèo");

        given(petRepository.findPetTypes()).willReturn(Arrays.asList(dog, cat));

        mvc.perform(get("/petTypes")
                .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentType("application/json"))
           .andExpect(jsonPath("$[0].id").value(1))
           .andExpect(jsonPath("$[0].name").value("Chó"))
           .andExpect(jsonPath("$[1].id").value(2))
           .andExpect(jsonPath("$[1].name").value("Mèo"));
    }

    @Test
    void shouldCreatePet() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Nguyễn");
        owner.setLastName("Đình Nhân");

        PetType dog = new PetType();
        dog.setId(1);
        dog.setName("Chó");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(petRepository.findPetTypeById(1)).willReturn(Optional.of(dog));
        given(petRepository.save(any(Pet.class))).will(invocation -> {
            Pet pet = invocation.getArgument(0);
            pet.setId(3);
            return pet;
        });

        String petRequest = "{\"id\":3,\"birthDate\":\"2023-04-01\",\"name\":\"Buddy\",\"typeId\":1}";

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petRequest))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(3))
           .andExpect(jsonPath("$.name").value("Buddy"))
           .andExpect(jsonPath("$.type.id").value(1));

        verify(ownerRepository).findById(1);
        verify(petRepository).findPetTypeById(1);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void shouldUpdatePet() throws Exception {
        Pet pet = new Pet();
        pet.setId(5);
        pet.setName("Mèo Mướp");
        pet.setBirthDate(new Date());

        PetType cat = new PetType();
        cat.setId(2);
        cat.setName("Mèo");
        pet.setType(cat);

        given(petRepository.findById(5)).willReturn(Optional.of(pet));

        String petRequest = "{\"id\":5,\"birthDate\":\"2023-04-01\",\"name\":\"Mèo Ráng\",\"typeId\":2}";

        mvc.perform(put("/owners/*/pets/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(petRequest))
           .andExpect(status().isNoContent());

        verify(petRepository).findById(5);
        verify(petRepository).save(pet);
    }

    @Test
    void shouldGetPetDetails() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Nguyễn");
        owner.setLastName("Đình Nhân");

        Pet pet = new Pet();
        pet.setId(7);
        pet.setName("Buddy");
        pet.setBirthDate(new Date());

        PetType dog = new PetType();
        dog.setId(1);
        dog.setName("Chó");
        pet.setType(dog);

        owner.addPet(pet);

        given(petRepository.findById(7)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/*/pets/7")
                .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(content().contentType("application/json"))
           .andExpect(jsonPath("$.id").value(7))
           .andExpect(jsonPath("$.name").value("Buddy"))
           .andExpect(jsonPath("$.owner").value("Nguyễn Đình Nhân"))
           .andExpect(jsonPath("$.type.id").value(1))
           .andExpect(jsonPath("$.type.name").value("Chó"));
    }

    @Test
    void shouldReturn404WhenPetNotFound() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/*/pets/999")
                .accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isNotFound())
           .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
    }
}