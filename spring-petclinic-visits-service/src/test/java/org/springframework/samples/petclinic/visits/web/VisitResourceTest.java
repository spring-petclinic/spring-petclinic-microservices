package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.http.MediaTypeAssert;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                                        .build()));

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
    void shouldReturnEmptyListWhenNoVisitsForMultiplePets() throws Exception {
        given(visitRepository.findByPetIdIn(List.of(999, 888))).willReturn(List.of());

        mvc.perform(get("/pets/visits?petId=999,888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void shouldFailWhenNoPetIdProvided() throws Exception {
        mvc.perform(get("/pets/visits"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateVisit() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
                .petId(101)
                .description("General checkup")
                .build();

        mvc.perform(post("/owners/*/pets/101/visits")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(visit)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFailWhenPetIdIsInvalid_1() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
                .petId(-1)
                .description("General checkup")
                .build();

        mvc.perform(post("/owners/*/pets/-1/visits")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(visit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenRequestBodyIsInvalid() throws Exception {
        mvc.perform(post("/owners/*/pets/101/visits")
                .contentType("application/json")
                .content("{invalid_json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFetchVisitsByPetId() throws Exception {
        given(visitRepository.findByPetId(111))
                .willReturn(List.of(
                        Visit.VisitBuilder.aVisit().id(10).petId(111).build()));

        mvc.perform(get("/owners/*/pets/111/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].petId").value(111));
    }

    @Test
    void shouldReturnEmptyListWhenNoVisits() throws Exception {
        given(visitRepository.findByPetId(99999999)).willReturn(List.of());

        mvc.perform(get("/owners/*/pets/99999999/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldFailWhenPetIdIsInvalid_2() throws Exception {
        mvc.perform(get("/owners/*/pets/0/visits"))
                .andExpect(status().isBadRequest());
    }
}
