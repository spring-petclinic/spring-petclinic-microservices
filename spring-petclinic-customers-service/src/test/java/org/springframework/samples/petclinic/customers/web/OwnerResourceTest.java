package org.springframework.samples.petclinic.customers.web;

import java.lang.reflect.Field;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class) // ✅ Loads only the web layer
@ActiveProfiles("test")
class OwnerResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerRepository ownerRepository;

    @MockBean // ✅ Mocking the missing dependency
    OwnerEntityMapper ownerEntityMapper;

    @Test
    void shouldGetAnOwnerInJsonFormat() throws Exception {
        Owner owner = setupOwner(); // ✅ Owner now has an ID

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1)) // ✅ ID is now correctly set
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.telephone").value("123456789"));
    }

    @Test
    void shouldCreateNewOwner() throws Exception {
        String newOwnerJson = """
        {
            "firstName": "Alice",
            "lastName": "Johnson",
            "address": "456 Oak Street",
            "city": "Metropolis",
            "telephone": "987654321"
        }
    """;

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newOwnerJson))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateOwner() throws Exception {
        Owner existingOwner = setupOwner(); // ✅ Set up an existing owner

        given(ownerRepository.findById(1)).willReturn(Optional.of(existingOwner));

        String updatedOwnerJson = """
        {
            "firstName": "Updated John",
            "lastName": "Updated Doe",
            "address": "789 Updated Street",
            "city": "Updated City",
            "telephone": "555555555"
        }
    """;

        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedOwnerJson))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailWhenCreatingOwnerWithMissingFields() throws Exception {
        String invalidOwnerJson = """
        {
            "firstName": "Alice"
        }
    """; // ❌ Missing lastName, address, city, and telephone

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOwnerJson))
            .andExpect(status().isBadRequest()); // ✅ Should return 400 Bad Request
    }

    private Owner setupOwner() throws Exception {
        Owner owner = new Owner();
        setId(owner, 1); // ✅ Set ID using reflection
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main Street");
        owner.setCity("Springfield");
        owner.setTelephone("123456789");
        return owner;
    }

    private void setId(Owner owner, int id) throws Exception {
        Field idField = Owner.class.getDeclaredField("id");
        idField.setAccessible(true); // Allow access to private field
        idField.set(owner, id);
    }
}
