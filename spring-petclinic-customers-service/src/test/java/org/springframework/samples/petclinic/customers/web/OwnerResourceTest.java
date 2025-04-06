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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PetRepository petRepository;

    @MockBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetAnOwner() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Rivera");
        owner.setLastName("Maria");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");

        given(ownerRepository.findById(1))
            .willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Rivera"))
            .andExpect(jsonPath("$.lastName").value("Maria"))
            .andExpect(jsonPath("$.address").value("123 Main St"))
            .andExpect(jsonPath("$.city").value("Springfield"));
    }

    @Test
    void shouldGetListOfOwners() throws Exception() {
        Owner owner1 = new Owner();
        owner1.setFirstName("Rivera");
        owner1.setLastName("Maria");
        owner1.setAddress("123 Main St");
        owner1.setCity("Springfield");

        Owner owner2 = new Owner();
        owner2.setFirstName("Smith");
        owner2.setLastName("John");
        owner2.setAddress("456 Elm St");
        owner2.setCity("Shelbyville");

        given(ownerRepository.findAll())
            .willReturn(List.of(owner1, owner2));

        mvc.perform(get("/owners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Rivera"))
            .andExpect(jsonPath("$[0].lastName").value("Maria"))
            .andExpect(jsonPath("$[0].address").value("123 Main St"))
            .andExpect(jsonPath("$[0].city").value("Springfield"))
            .andExpect(jsonPath("$[1].firstName").value("Smith"))
            .andExpect(jsonPath("$[1].lastName").value("John"))
            .andExpect(jsonPath("$[1].address").value("456 Elm St"))
            .andExpect(jsonPath("$[1].city").value("Shelbyville"));
    }
};