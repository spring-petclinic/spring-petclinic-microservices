package org.springframework.samples.petclinic.customers.web;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;// Add this import if the class exists in your project
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
class OwnerResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private OwnerEntityMapper ownerEntityMapper;

    @Test
    void shouldCreateOwner() throws Exception {
        mockMvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Street\",\"city\":\"City\",\"telephone\":\"1234567890\"}"))
                .andExpect(status().isCreated());
    }
    
    @Test
    void shouldFindAllOwners() throws Exception {
        Owner owner1 = new Owner();
        owner1.setFirstName("John");
        owner1.setLastName("Doe");

        Owner owner2 = new Owner();
        owner2.setFirstName("Jane");
        owner2.setLastName("Smith");

        given(ownerRepository.findAll()).willReturn(Arrays.asList(owner1, owner2));

        mockMvc.perform(get("/owners"))
                .andExpect(status().isOk());
    }
    @Test
    void shouldUpdateOwner() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mockMvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Street\",\"city\":\"City\",\"telephone\":\"1234567890\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingOwner() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.empty());

        mockMvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Street\",\"city\":\"City\",\"telephone\":\"1234567890\"}"))
                .andExpect(status().isNotFound());
                
    }
}