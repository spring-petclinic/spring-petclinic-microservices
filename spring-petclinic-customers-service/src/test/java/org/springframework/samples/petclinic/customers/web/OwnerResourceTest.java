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
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerEntityMapper ownerEntityMapper;

    @MockBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetAnOwner() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Rivera");
        owner.setLastName("Maria");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("0332120108");

        given(ownerRepository.findById(1))
            .willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Rivera"))
            .andExpect(jsonPath("$.lastName").value("Maria"))
            .andExpect(jsonPath("$.address").value("123 Main St"))
            .andExpect(jsonPath("$.city").value("Springfield"));
    }

    @Test
    void shouldGetListOfOwners() throws Exception {
        Owner owner1 = new Owner();
        owner1.setFirstName("Rivera");
        owner1.setLastName("Maria");
        owner1.setAddress("123 Main St");
        owner1.setCity("Springfield");
        owner1.setTelephone("0332120108");

        Owner owner2 = new Owner();
        owner2.setFirstName("Smith");
        owner2.setLastName("John");
        owner2.setAddress("456 Elm St");
        owner2.setCity("Shelbyville");
        owner2.setTelephone("0332120789");

        given(ownerRepository.findAll())
            .willReturn(asList(owner1, owner2));

        mvc.perform(get("/owners").accept(MediaType.APPLICATION_JSON))
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

    @Test
    void createOwner_validOwner_shouldReturnCreated() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Rivera");
        owner.setLastName("Maria");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("0332120108");

        given(ownerRepository.save(owner)).willReturn(owner);

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Rivera\",\"lastName\":\"Maria\",\"address\":\"123 Main St\",\"city\":\"Springfield\",\"telephone\":\"0332120108\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void updateOwner_validOwnerId_shouldReturnNoContent() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Rivera");
        owner.setLastName("Maria");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("0332120108");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Rivera\",\"lastName\":\"Maria\",\"address\":\"123 Main St\",\"city\":\"Springfield\",\"telephone\":\"0332120108\"}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateOwner_invalidOwnerId_shouldReturnNotFound() throws Exception {
        int nonExistentOwnerId = 999;
        given(ownerRepository.findById(nonExistentOwnerId)).willReturn(Optional.empty());

        mvc.perform(put("/owners/{ownerId}", nonExistentOwnerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Rivera\",\"lastName\":\"Maria\",\"address\":\"123 Main St\",\"city\":\"Springfield\",\"telephone\":\"0332120108\"}"))
            .andExpect(status().isNotFound());
    }
};