package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetResourceTest {

    private MockMvc mockMvc;

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private PetResource petResource;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petResource).build();
    }

    @Test
    void shouldCreatePet_WhenValidRequest() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(dateFormat.parse("2000-01-01"));

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(petRepository.save(any(Pet.class))).willReturn(pet);

        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "name": "Buddy",
                    "birthDate": "2020-01-01",
                    "typeId": 2
                }
                """))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdatePet_WhenValidRequest() throws Exception {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(dateFormat.parse("2000-01-01"));

        given(petRepository.findById(1)).willReturn(Optional.of(pet));
        given(petRepository.save(any(Pet.class))).willReturn(pet);

        mockMvc.perform(put("/owners/*/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "id": 1,
                    "name": "Max",
                    "birthDate": "2021-05-05",
                    "typeId": 3
                }
                """))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnPetTypes_WhenRequested() throws Exception {
        PetType petType1 = new PetType();
        petType1.setId(1);
        petType1.setName("Dog");

        PetType petType2 = new PetType();
        petType2.setId(2);
        petType2.setName("Cat");

        given(petRepository.findPetTypes()).willReturn(List.of(petType1, petType2));

        mockMvc.perform(get("/petTypes")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Dog"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Cat"));
    }

    @Test
    void shouldReturnPetDetails_WhenPetExists() throws Exception {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setBirthDate(dateFormat.parse("2000-01-01"));

        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("Jack");
        owner.setLastName("Smith");

        pet.setOwner(owner);

        PetDetails petDetails = new PetDetails(pet);

        given(petRepository.findById(1)).willReturn(Optional.of(pet));

        mockMvc.perform(get("/owners/*/pets/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void shouldReturnNotFound_WhenPetDoesNotExist() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mockMvc.perform(get("/owners/*/pets/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    @Test
    void shouldReturnBadRequest_WhenCreatingPetWithInvalidData() throws Exception {
        mockMvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "name": "",
                    "birthDate": "",
                    "typeId": null
                }
                """))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldReturnNotFound_WhenCreatingPetForNonExistentOwner() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mockMvc.perform(post("/owners/999/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "name": "Buddy",
                    "birthDate": "2020-01-01",
                    "typeId": 2
                }
                """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound_WhenUpdatingNonExistentPet() throws Exception {
        given(petRepository.findById(999)).willReturn(Optional.empty());

        mockMvc.perform(put("/owners/*/pets/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                    "id": 999,
                    "name": "Max",
                    "birthDate": "2021-05-05",
                    "typeId": 3
                }
                """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyList_WhenNoPetTypesExist() throws Exception {
        given(petRepository.findPetTypes()).willReturn(List.of());

        mockMvc.perform(get("/petTypes")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

}
