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


import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Test
    void shouldFetchVisits() throws Exception {
        given(visitRepository.findByPetIdIn(asList(111, 222)))
            .willReturn(
                asList(
                    Visit.VisitBuilder.aVisit()
                        .id(1)
                        .petId(111)
                        .build(),
                    Visit.VisitBuilder.aVisit()
                        .id(2)
                        .petId(222)
                        .build(),
                    Visit.VisitBuilder.aVisit()
                        .id(3)
                        .petId(222)
                        .build()
                )
            );

        mvc.perform(get("/pets/visits?petId=111,222"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.items[1].id").value(2))
            .andExpect(jsonPath("$.items[2].id").value(3))
            .andExpect(jsonPath("$.items[0].petId").value(111))
            .andExpect(jsonPath("$.items[1].petId").value(222))
            .andExpect(jsonPath("$.items[2].petId").value(222));
    }

    @Test
    void createVisit_shouldReturnCreatedVisit() throws Exception {
        Visit visit = new Visit();
        visit.setPetId(1);
        visit.setDescription("Checkup");

        Visit savedVisit = new Visit();
        savedVisit.setPetId(1);
        savedVisit.setDescription("Checkup");

        when(visitRepository.save(any(Visit.class))).thenReturn(savedVisit);

        mvc.perform(post("/owners/123/pets/1/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"description\": \"Checkup\" }"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description").value("Checkup"));
    }

    @Test
    void readVisitsByPetId_shouldReturnVisitList() throws Exception {
        Visit visit = new Visit();
        visit.setPetId(1);
        visit.setDescription("Dental");

        when(visitRepository.findByPetId(1)).thenReturn(List.of(visit));

        mvc.perform(get("/owners/123/pets/1/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].description").value("Dental"));
    }

    @Test
    void readMultipleVisits_shouldReturnVisitsWrapper() throws Exception {
        Visit visit1 = new Visit();
        visit1.setPetId(1);
        visit1.setDescription("Visit 1");

        Visit visit2 = new Visit();
        visit2.setPetId(2);
        visit2.setDescription("Visit 2");

        when(visitRepository.findByPetIdIn(List.of(1, 2))).thenReturn(List.of(visit1, visit2));

        mvc.perform(get("/pets/visits")
                .param("petId", "1", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].description").value("Visit 1"))
            .andExpect(jsonPath("$.items[1].description").value("Visit 2"));
    }
}
