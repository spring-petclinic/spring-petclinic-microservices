package org.springframework.samples.petclinic.customers.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
public class OwnerResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OwnerRepository ownerRepository;

    @MockBean
    private OwnerEntityMapper ownerEntityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // Test cho endpoint tạo Owner: POST /owners
    @Test
    void shouldCreateOwner() throws Exception {
        // Tạo OwnerRequest với dữ liệu mẫu (theo định nghĩa record trong OwnerRequest.java :contentReference[oaicite:0]{index=0})
        OwnerRequest ownerRequest = new OwnerRequest("John", "Doe", "123 Main St", "Springfield", "1234567890");
        
        // Tạo đối tượng Owner và thiết lập các field cần thiết qua ReflectionTestUtils
        Owner owner = new Owner();
        ReflectionTestUtils.setField(owner, "id", 1);
        ReflectionTestUtils.setField(owner, "firstName", "John");
        ReflectionTestUtils.setField(owner, "lastName", "Doe");

        // Stub hành vi của mapper và repository
        given(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class))).willReturn(owner);
        given(ownerRepository.save(any(Owner.class))).willReturn(owner);
        
        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    // Test cho endpoint đọc một Owner: GET /owners/{ownerId}
    @Test
    void shouldFindOwnerById() throws Exception {
        Owner owner = new Owner();
        ReflectionTestUtils.setField(owner, "id", 1);
        ReflectionTestUtils.setField(owner, "firstName", "Alice");
        ReflectionTestUtils.setField(owner, "lastName", "Smith");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        
        mvc.perform(get("/owners/1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    // Test cho endpoint đọc danh sách Owner: GET /owners
    @Test
    void shouldFindAllOwners() throws Exception {
        Owner owner1 = new Owner();
        ReflectionTestUtils.setField(owner1, "id", 1);
        ReflectionTestUtils.setField(owner1, "firstName", "Alice");
        ReflectionTestUtils.setField(owner1, "lastName", "Smith");

        Owner owner2 = new Owner();
        ReflectionTestUtils.setField(owner2, "id", 2);
        ReflectionTestUtils.setField(owner2, "firstName", "Bob");
        ReflectionTestUtils.setField(owner2, "lastName", "Johnson");

        given(ownerRepository.findAll()).willReturn(Arrays.asList(owner1, owner2));
        
        mvc.perform(get("/owners")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    // Test cho endpoint cập nhật Owner: PUT /owners/{ownerId}
    @Test
    void shouldUpdateOwner() throws Exception {
        OwnerRequest ownerRequest = new OwnerRequest("Charlie", "Brown", "456 Park Ave", "Metropolis", "0987654321");
        
        Owner existingOwner = new Owner();
        ReflectionTestUtils.setField(existingOwner, "id", 1);
        ReflectionTestUtils.setField(existingOwner, "firstName", "Old");
        ReflectionTestUtils.setField(existingOwner, "lastName", "Name");
        
        given(ownerRepository.findById(1)).willReturn(Optional.of(existingOwner));
        // Giả lập hành vi mapping cập nhật thông tin cho owner
        given(ownerEntityMapper.map(existingOwner, ownerRequest)).willReturn(existingOwner);
        given(ownerRepository.save(existingOwner)).willReturn(existingOwner);
        
        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ownerRequest)))
                .andExpect(status().isNoContent());
    }
}
