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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void shouldFetchVisitsByPetId() throws Exception {
        given(visitRepository.findByPetId(111))
            .willReturn(
                asList(
                    Visit.VisitBuilder.aVisit()
                        .id(1)
                        .petId(111)
                        .description("Regular checkup")
                        .build(),
                    Visit.VisitBuilder.aVisit()
                        .id(2)
                        .petId(111)
                        .description("Vaccination")
                        .build()
                )
            );

        mvc.perform(get("/owners/1/pets/111/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[0].petId").value(111))
            .andExpect(jsonPath("$[1].petId").value(111))
            .andExpect(jsonPath("$[0].description").value("Regular checkup"))
            .andExpect(jsonPath("$[1].description").value("Vaccination"));
    }

    @Test
    void shouldReturnEmptyListWhenNoVisitsFound() throws Exception {
        given(visitRepository.findByPetId(999))
            .willReturn(emptyList());

        mvc.perform(get("/owners/1/pets/999/visits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldCreateNewVisit() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
            .description("New visit")
            .build();

        Visit savedVisit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(111)
            .description("New visit")
            .build();

        given(visitRepository.save(any(Visit.class)))
            .willReturn(savedVisit);

        mvc.perform(post("/owners/1/pets/111/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"New visit\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(111))
            .andExpect(jsonPath("$.description").value("New visit"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPetId() throws Exception {
        mvc.perform(get("/owners/1/pets/0/visits"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForMissingPetIdParameter() throws Exception {
        mvc.perform(get("/pets/visits"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateVisitWithEmptyDescription() throws Exception {
        Visit savedVisit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(111)
            .description("")
            .build();

        given(visitRepository.save(any(Visit.class)))
            .willReturn(savedVisit);

        mvc.perform(post("/owners/1/pets/111/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(111))
            .andExpect(jsonPath("$.description").value(""));
    }
}
