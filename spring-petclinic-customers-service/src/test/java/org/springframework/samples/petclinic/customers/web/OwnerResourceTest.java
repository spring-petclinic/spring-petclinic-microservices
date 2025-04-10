package org.springframework.samples.petclinic.customers.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
/**
 * @author Hoang Tien Huy
 */
public class OwnerResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerRepository ownerRepository;

    @MockBean
    OwnerEntityMapper ownerEntityMapper;

    //test get owner by id
    @Test
    void findOwner_whenOwnerExists_returnOwner() throws Exception {
        Owner owner = setupOwner();
        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(owner.getId()))
            .andExpect(jsonPath("$.firstName").value(owner.getFirstName()));
    }
    @Test
    void findOwner_whenOwnerNotFound_returnEmpty() throws Exception {
        given(ownerRepository.findById(99)).willReturn(Optional.empty());

        mvc.perform(get("/owners/99"))
            .andExpect(status().isOk())
            .andExpect(content().string("null"));
    }

    //test create owner
    @Test
    void createOwner_whenValidRequest_returnCreatedOwner() throws Exception {
        Owner owner = setupOwner();
        int ownerId = 1;
        OwnerRequest ownerRequest = setupOwnerRequest();
        given(ownerEntityMapper.map(any(), any())).willReturn(owner);
        given(ownerRepository.save(any())).willReturn(owner);

        String json = new ObjectMapper().writeValueAsString(ownerRequest);

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value(owner.getFirstName()))
            .andExpect(jsonPath("$.lastName").value(owner.getLastName()))
            .andExpect(jsonPath("$.address").value(owner.getAddress()))
            .andExpect(jsonPath("$.city").value(owner.getCity()))
            .andExpect(jsonPath("$.telephone").value(owner.getTelephone()));
    }
    @Test
    void createOwner_whenInvalidRequest_returnBadRequest() throws Exception {
        OwnerRequest ownerRequest = new OwnerRequest("", "", "", "", "");

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ownerRequest)))
            .andExpect(status().isBadRequest());
    }

    //test update owner
    @Test
    void updateOwner_whenValidRequest_returnUpdatedOwner() throws Exception {
        Owner owner = setupOwner();
        int ownerId = 1;
        OwnerRequest ownerRequest = setupOwnerRequest();
        given(ownerRepository.findById(ownerId)).willReturn(Optional.of(owner));
        given(ownerEntityMapper.map(any(), any())).willReturn(owner);
        given(ownerRepository.save(any())).willReturn(owner);

        String json = new ObjectMapper().writeValueAsString(ownerRequest);

        mvc.perform(put("/owners/{ownerId}", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isNoContent());
    }
    @Test
    void updateOwner_whenOwnerNotFound_returnNotFound() throws Exception {
        int ownerId = 99;
        OwnerRequest ownerRequest = setupOwnerRequest();
        given(ownerRepository.findById(ownerId)).willReturn(Optional.empty());

        mvc.perform(put("/owners/{ownerId}", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ownerRequest)))
            .andExpect(status().isNotFound());
    }
    @Test
    void updateOwner_whenInvalidRequest_returnBadRequest() throws Exception {
        int ownerId = 1;
        OwnerRequest ownerRequest = new OwnerRequest("", "", "", "", "");

        mvc.perform(put("/owners/{ownerId}", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(ownerRequest)))
            .andExpect(status().isBadRequest());
    }

    private Owner setupOwner() {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("New York");
        owner.setTelephone("123456237890");
        return owner;
    }

    private OwnerRequest setupOwnerRequest() {
        return new OwnerRequest("John", "Doe", "123 Main St",
            "New York","123345627890");
    }
}
