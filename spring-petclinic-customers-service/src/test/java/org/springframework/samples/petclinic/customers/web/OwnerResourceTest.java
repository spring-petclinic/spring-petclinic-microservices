package org.springframework.samples.petclinic.customers.web;

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

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerRepository ownerRepository;
    
    @MockBean
    OwnerEntityMapper ownerEntityMapper;

    @Test
    void testGetOwnerById() throws Exception {
        Owner owner = new Owner();
        //owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("New York");
        owner.setTelephone("1234567890");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.address").value("123 Main St"))
            .andExpect(jsonPath("$.city").value("New York"))
            .andExpect(jsonPath("$.telephone").value("1234567890"));
    }

    @Test
    void testGetOwnerNotFound() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void testCreateOwner() throws Exception {
        Owner owner = new Owner();
        //owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("New York");
        owner.setTelephone("1234567890");

        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"New York\",\"telephone\":\"1234567890\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testUpdateOwner() throws Exception {
        Owner existingOwner = new Owner();
        //existingOwner.setId(1);
        existingOwner.setFirstName("John");
        existingOwner.setLastName("Doe");
        existingOwner.setAddress("123 Main St");
        existingOwner.setCity("New York");
        existingOwner.setTelephone("1234567890");

        Owner updatedOwner = new Owner();
        //updatedOwner.setId(1);
        updatedOwner.setFirstName("Jane");
        updatedOwner.setLastName("Doe");
        updatedOwner.setAddress("456 Oak St");
        updatedOwner.setCity("Boston");
        updatedOwner.setTelephone("0987654321");

        given(ownerRepository.findById(1)).willReturn(Optional.of(existingOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(updatedOwner);

        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"address\":\"456 Oak St\",\"city\":\"Boston\",\"telephone\":\"0987654321\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.address").value("456 Oak St"));
    }
}