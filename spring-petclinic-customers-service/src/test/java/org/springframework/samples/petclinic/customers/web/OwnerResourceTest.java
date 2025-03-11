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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
class OwnerResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private OwnerEntityMapper ownerEntityMapper;

    @Test
    void shouldCreateOwner() throws Exception {
        OwnerRequest request = new OwnerRequest("John", "Doe", "123 Main St", "New York", "1234567890");
        Owner owner = new Owner();

        given(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class))).willReturn(owner);
        given(ownerRepository.save(any(Owner.class))).willReturn(owner);

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"New York\",\"telephone\":\"1234567890\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldFindOwnerById() throws Exception {
        Owner owner = new Owner(); // Do NOT set ID manually
        owner.setFirstName("Alice");
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void shouldFindAllOwners() throws Exception {
        Owner owner1 = new Owner();
        owner1.setFirstName("Alice");

        Owner owner2 = new Owner();
        owner2.setFirstName("Bob");

        given(ownerRepository.findAll()).willReturn(List.of(owner1, owner2));

        mvc.perform(get("/owners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Alice"))
            .andExpect(jsonPath("$[1].firstName").value("Bob"));
    }

    @Test
    void shouldUpdateOwner() throws Exception {
        Owner existingOwner = new Owner();
        existingOwner.setFirstName("OldName");

        OwnerRequest request = new OwnerRequest("UpdatedName", "Doe", "456 Elm St", "Los Angeles", "9876543210");

        given(ownerRepository.findById(1)).willReturn(Optional.of(existingOwner));
        given(ownerRepository.save(any(Owner.class))).willReturn(existingOwner);
        given(ownerEntityMapper.map(existingOwner, request)).willReturn(existingOwner); // ✅ Fix `doNothing()`

        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"UpdatedName\",\"lastName\":\"Doe\",\"address\":\"456 Elm St\",\"city\":\"Los Angeles\",\"telephone\":\"9876543210\"}"))
            .andExpect(status().isNoContent());
    }
}
