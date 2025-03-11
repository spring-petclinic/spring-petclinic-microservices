package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
class PetResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PetRepository petRepository;

    @MockBean
    private OwnerRepository ownerRepository;

    @Test
    void shouldCreatePet() throws Exception {
        Owner owner = new Owner();
        Pet pet = createMockPet(1, "Buddy", new Date(), new PetType(), owner);

        PetRequest request = new PetRequest(1, new Date(), "Buddy", 2);

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        // ✅ Ensure the saved pet has an ID and an owner
        given(petRepository.save(any(Pet.class))).willAnswer(invocation -> {
            Pet savedPet = invocation.getArgument(0);
            return createMockPet(1, savedPet.getName(), savedPet.getBirthDate(), savedPet.getType(), savedPet.getOwner());
        });

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"birthDate\":\"2023-01-01\",\"name\":\"Buddy\",\"typeId\":2}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void shouldUpdatePet() throws Exception {
        Owner owner = new Owner();
        Pet existingPet = createMockPet(1, "Buddy", new Date(), new PetType(), owner);

        PetRequest request = new PetRequest(1, new Date(), "Max", 2);

        given(petRepository.findById(1)).willReturn(Optional.of(existingPet));
        given(petRepository.save(any(Pet.class))).willReturn(existingPet);

        mvc.perform(put("/owners/*/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"birthDate\":\"2023-01-01\",\"name\":\"Max\",\"typeId\":2}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindPetById() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        Pet pet = createMockPet(1, "Buddy", new Date(), new PetType(), owner);

        given(petRepository.findById(1)).willReturn(Optional.of(pet));

        mvc.perform(get("/owners/*/pets/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void shouldReturnPetTypes() throws Exception {
        PetType type1 = new PetType();
        type1.setName("Dog");

        PetType type2 = new PetType();
        type2.setName("Cat");

        given(petRepository.findPetTypes()).willReturn(List.of(type1, type2));

        mvc.perform(get("/petTypes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Dog"))
            .andExpect(jsonPath("$[1].name").value("Cat"));
    }

    // ✅ Utility method to create a Pet instance with an ID and an Owner
    private Pet createMockPet(Integer id, String name, Date birthDate, PetType type, Owner owner) {
        return new Pet() {
            @Override
            public Integer getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Date getBirthDate() {
                return birthDate;
            }

            @Override
            public PetType getType() {
                return type;
            }

            @Override
            public Owner getOwner() {
                return owner;
            }
        };
    }

    @Test
    void shouldNotUpdateNonExistingPet() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(put("/owners/*/pets/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"birthDate\":\"2023-01-01\",\"name\":\"Ghost\",\"typeId\":3}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForMissingPet() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/*/pets/999"))
            .andExpect(status().isNotFound());
    }
}
