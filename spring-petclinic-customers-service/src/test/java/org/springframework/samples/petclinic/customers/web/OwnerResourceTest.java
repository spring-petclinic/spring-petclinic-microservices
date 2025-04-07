package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerResource.class)
class OwnerResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private OwnerEntityMapper ownerEntityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Owner owner;
    private OwnerRequest ownerRequest;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        // Khởi tạo record
        ownerRequest = new OwnerRequest(
            "John",
            "Doe",
            "123 Street",
            "Cityville",
            "123456789"
        );
    }

    @Test
    void testCreateOwner() throws Exception {
        when(ownerEntityMapper.map(any(Owner.class), eq(ownerRequest))).thenReturn(owner);
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void testFindOwnerById() throws Exception {
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testFindAllOwners() throws Exception {
        when(ownerRepository.findAll()).thenReturn(Arrays.asList(owner));

        mockMvc.perform(get("/owners"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testUpdateOwner() throws Exception {
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);
        when(ownerEntityMapper.map(any(Owner.class), eq(ownerRequest))).thenReturn(owner);

        mockMvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
            .andExpect(status().isNoContent());

        verify(ownerRepository).save(owner);
    }

    @Test
    void testUpdateOwnerNotFound() throws Exception {
        when(ownerRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(put("/owners/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
            .andExpect(status().isNotFound());
    }
}
