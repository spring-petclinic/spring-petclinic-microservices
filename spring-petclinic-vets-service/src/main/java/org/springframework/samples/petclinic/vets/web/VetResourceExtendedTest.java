package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceExtendedTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VetRepository vetRepository;

    @Test
    void shouldGetAListOfVetsWithSpecialties() throws Exception {
        Specialty radiology = new Specialty();
        //radiology.setId(1);
        radiology.setName("radiology");

        Specialty surgery = new Specialty();
        //surgery.setId(2);
        surgery.setName("surgery");

        Vet vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("James");
        vet1.setLastName("Carter");

        Vet vet2 = new Vet();
        vet2.setId(2);
        vet2.setFirstName("Helen");
        vet2.setLastName("Leary");
        vet2.addSpecialty(radiology);

        Vet vet3 = new Vet();
        vet3.setId(3);
        vet3.setFirstName("Linda");
        vet3.setLastName("Douglas");
        vet3.addSpecialty(surgery);
        vet3.addSpecialty(radiology);

        List<Vet> vets = Arrays.asList(vet1, vet2, vet3);
        given(vetRepository.findAll()).willReturn(vets);

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[0].lastName").value("Carter"))
            .andExpect(jsonPath("$[0].specialties.length()").value(0))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Helen"))
            .andExpect(jsonPath("$[1].lastName").value("Leary"))
            .andExpect(jsonPath("$[1].specialties.length()").value(1))
            .andExpect(jsonPath("$[1].specialties[0].name").value("radiology"))
            .andExpect(jsonPath("$[2].id").value(3))
            .andExpect(jsonPath("$[2].firstName").value("Linda"))
            .andExpect(jsonPath("$[2].lastName").value("Douglas"))
            .andExpect(jsonPath("$[2].specialties.length()").value(2));
    }

    @Test
    void shouldReturnEmptyListWhenNoVets() throws Exception {
        given(vetRepository.findAll()).willReturn(List.of());

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldVerifyCachingHeader() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        given(vetRepository.findAll()).willReturn(List.of(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(header().exists("Cache-Control"));
    }
}