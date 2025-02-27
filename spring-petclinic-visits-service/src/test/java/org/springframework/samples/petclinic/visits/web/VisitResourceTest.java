package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Test
    void shouldCreateVisit() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)  // Ensure ID is present
            .description("General Checkup")
            .petId(111)
            .build();

        Visit savedVisit = Visit.VisitBuilder.aVisit()
            .id(1)  // The repository should return a saved visit with ID
            .description("General Checkup")
            .petId(111)
            .build();

        given(visitRepository.save(org.mockito.ArgumentMatchers.any(Visit.class)))
            .willReturn(savedVisit);  // Ensure returned object has an ID

        mvc.perform(post("/owners/*/pets/111/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"General Checkup\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("General Checkup"))
            .andExpect(jsonPath("$.petId").value(111));
    }


    @Test
    void shouldFetchVisitsByPetId() throws Exception {
        List<Visit> visits = Arrays.asList(
            Visit.VisitBuilder.aVisit().id(1).petId(111).description("Checkup").build(),
            Visit.VisitBuilder.aVisit().id(2).petId(111).description("Vaccination").build()
        );

        given(visitRepository.findByPetId(111)).willReturn(visits);

        mvc.perform(get("/owners/*/pets/111/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].description").value("Checkup"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].description").value("Vaccination"));
    }

    @Test
    void shouldFetchVisitsByMultiplePetIds() throws Exception {
        List<Visit> visits = Arrays.asList(
            Visit.VisitBuilder.aVisit().id(1).petId(111).description("Checkup").build(),
            Visit.VisitBuilder.aVisit().id(2).petId(112).description("Surgery").build()
        );

        given(visitRepository.findByPetIdIn(Arrays.asList(111, 112))).willReturn(visits);

        mvc.perform(get("/pets/visits")
                .param("petId", "111", "112"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.items[0].description").value("Checkup"))
            .andExpect(jsonPath("$.items[1].id").value(2))
            .andExpect(jsonPath("$.items[1].description").value("Surgery"));
    }
}
