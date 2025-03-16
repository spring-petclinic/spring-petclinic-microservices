package org.springframework.samples.petclinic.customers.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@WebMvcTest(OwnerResource.class)
class OwnerResourceTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository ownerRepository;

    @Test
    void testGetOwnerById_ShouldReturnOwner() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Tom");
        owner.setLastName("Doe");
        owner.setAddress("123 Street");
        owner.setCity("City");
        owner.setTelephone("123456789");

        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testGetOwnerById_ShouldReturnNotFound() throws Exception {
        when(ownerRepository.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/owners/999"))
            .andExpect(status().isNotFound());
    }
}
