package org.springframework.samples.petclinic.customers.web;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author Maciej Szarlinski
 */
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PetRepository petRepository;

    @MockitoBean
    OwnerRepository ownerRepository;

    @Test
    void shouldGetAPetInJSonFormat() throws Exception {

        Pet pet = setupPet();

        given(petRepository.findById(2)).willReturn(Optional.of(pet));


        mvc.perform(get("/owners/2/pets/2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    @Test
    void shouldReturnNotFoundWhenPetDoesNotExist() throws Exception {
        given(petRepository.findById(99)).willReturn(Optional.empty());

        mvc.perform(get("/owners/2/pets/99").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectFuturePetBirthdayOnUpdate() throws Exception{
        String futureDate = LocalDate.now().plusYears(1).toString();
        String futureBirthDateJson = """
        {"name": "Basil", "typeId": 6, "birthDate": "%s"}
        """.formatted(futureDate);

        mvc.perform(put("/owners/2/pets/2")
            .contentType(MediaType.APPLICATION_JSON)
            .content(futureBirthDateJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFuturePetBirthdayOnPost() throws Exception{
        String futureDate = LocalDate.now().plusYears(1).toString();
        String futureBirthDateJson = """
        {"name": "Basil", "typeId": 6, "birthDate": "%s"}
        """.formatted(futureDate);

        mvc.perform(post("/owners/2/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(futureBirthDateJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePetUsingPathIdWithoutBodyId() throws Exception {
        Pet pet = setupPet();
        given(petRepository.findById(2)).willReturn(Optional.of(pet));
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(pet.getType()));

        mvc.perform(put("/owners/2/pets/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Updated", "typeId": 6, "birthDate": "2020-01-01"}
                    """))
            .andExpect(status().isNoContent());

        verify(petRepository).findById(2);
        verify(petRepository).save(pet);
        assertThat(pet.getId()).isEqualTo(2);
        assertThat(pet.getName()).isEqualTo("Updated");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentPathPet() throws Exception {
        given(petRepository.findById(99)).willReturn(Optional.empty());

        mvc.perform(put("/owners/2/pets/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Basil", "typeId": 6, "birthDate": "2020-01-01"}
                    """))
            .andExpect(status().isNotFound());

        verify(petRepository).findById(99);
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void shouldCreatePetWithoutBodyId() throws Exception {
        Pet existingPet = setupPet();
        given(ownerRepository.findById(2)).willReturn(Optional.of(existingPet.getOwner()));
        given(petRepository.findPetTypeById(6)).willReturn(Optional.of(existingPet.getType()));
        given(petRepository.save(any(Pet.class))).willAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(post("/owners/2/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "New pet", "typeId": 6, "birthDate": "2020-01-01"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("New pet"))
            .andExpect(jsonPath("$.type.id").value(6));
    }

    private Pet setupPet() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        Pet pet = new Pet();

        pet.setName("Basil");
        pet.setId(2);

        PetType petType = new PetType();
        petType.setId(6);
        pet.setType(petType);

        owner.addPet(pet);
        return pet;
    }
}
