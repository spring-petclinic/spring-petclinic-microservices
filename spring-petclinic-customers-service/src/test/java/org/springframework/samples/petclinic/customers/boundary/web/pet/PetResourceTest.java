package org.springframework.samples.petclinic.customers.boundary.web.pet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.application.OwnerService;
import org.springframework.samples.petclinic.customers.application.PetService;
import org.springframework.samples.petclinic.customers.domain.model.owner.Owner;
import org.springframework.samples.petclinic.customers.domain.model.pet.Pet;
import org.springframework.samples.petclinic.customers.domain.model.pet.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Maciej Szarlinski
 */
@RunWith(SpringRunner.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
public class PetResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    PetService petService;

    @MockBean
    OwnerService ownerService;

    @Test
    public void shouldGetAPetInJSonFormat() throws Exception {

        Pet pet = setupPet();

        given(petService.findPetById(2)).willReturn(pet);


        mvc.perform(get("/owners/2/pets/2.json").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Basil"))
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
