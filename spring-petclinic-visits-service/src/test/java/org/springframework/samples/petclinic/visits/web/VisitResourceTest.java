package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;
    // test

    @Test
    void shouldCreateVisit() throws Exception {
        Visit visit = new Visit();
        visit.setPetId(1);
        visit.setDescription("Regular checkup");

        given(visitRepository.save(any(Visit.class))).willReturn(visit);
        // test

        mvc.perform(post("/owners/*/pets/1/visits")
                .content("{\"petId\": 1, \"description\": \"Regular checkup\"}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateVisitWithNullDate() throws Exception {
        Visit visit = new Visit();
        visit.setPetId(1);
        visit.setDescription("Regular checkup");

        given(visitRepository.save(any(Visit.class))).willReturn(visit);

        mvc.perform(post("/owners/*/pets/1/visits")
                .content("{\"petId\": 1, \"description\": \"Regular checkup\", \"date\": null}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectVisitWhenRepositoryThrowsException() throws Exception {
        given(visitRepository.save(any(Visit.class))).willThrow(new IllegalArgumentException("Invalid visit data"));

        mvc.perform(post("/owners/*/pets/1/visits")
                .content("{\"petId\": 1, \"description\": \"Regular checkup\"}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectVisitWithInvalidPetId() throws Exception {
        mvc.perform(post("/owners/*/pets/0/visits")
                .content("{\"petId\": 0, \"description\": \"Regular checkup\"}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectVisitWithTooLongDescription() throws Exception {
        String tooLongDescription = String.join("", Collections.nCopies(8193, "a"));
        mvc.perform(post("/owners/*/pets/1/visits")
                .content("{\"petId\": 1, \"description\": \"" + tooLongDescription + "\"}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFetchVisits() throws Exception {
        Visit visit = new Visit();
        visit.setPetId(1);
        visit.setDescription("Regular checkup");

        given(visitRepository.findByPetId(1)).willReturn(Collections.singletonList(visit));

        mvc.perform(get("/owners/*/pets/1/visits")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].description").value("Regular checkup"));
    }

    @Test
    void shouldReturnNotFoundForNonExistingPetVisits() throws Exception {
        given(visitRepository.findByPetId(1)).willReturn(Collections.emptyList());

        mvc.perform(get("/owners/*/pets/1/visits")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectNegativePetId() throws Exception {
        mvc.perform(get("/owners/*/pets/-1/visits")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFetchVisitsForMultiplePets() throws Exception {
        Visit visit1 = new Visit();
        visit1.setPetId(1);
        visit1.setDescription("Regular checkup");

        Visit visit2 = new Visit();
        visit2.setPetId(2);
        visit2.setDescription("Emergency visit");

        List<Visit> visits = Arrays.asList(visit1, visit2);
        given(visitRepository.findByPetIdIn(Arrays.asList(1, 2))).willReturn(visits);

        mvc.perform(get("/pets/visits?petId=1,2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].description").value("Regular checkup"))
            .andExpect(jsonPath("$.items[1].description").value("Emergency visit"));
    }

    @Test
    void shouldRejectEmptyPetIdsList() throws Exception {
        mvc.perform(get("/pets/visits")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidPetIdInList() throws Exception {
        mvc.perform(get("/pets/visits?petId=1,0,2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
