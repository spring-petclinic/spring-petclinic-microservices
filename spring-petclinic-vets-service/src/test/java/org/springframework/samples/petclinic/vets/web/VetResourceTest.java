package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Collections.emptyList;
import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VetRepository vetRepository;

    @Test
    void shouldGetAListOfVets() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("Alice");
        vet.setLastName("Brown");

        given(vetRepository.findAll()).willReturn(asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("Alice"))
            .andExpect(jsonPath("$[0].lastName").value("Brown"));
    }

    @Test
    void shouldReturnEmptyListWhenNoVetsAvailable() throws Exception {
        given(vetRepository.findAll()).willReturn(emptyList());

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }
}
