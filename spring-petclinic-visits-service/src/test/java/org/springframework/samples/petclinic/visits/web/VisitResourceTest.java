package org.springframework.samples.petclinic.visits.web;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


import org.springframework.http.MediaType;
import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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
        Date date = new Date();

        given(visitRepository.findByPetIdIn(asList(111, 222)))
            .willReturn(
                asList(
                    Visit.VisitBuilder.aVisit()
                        .id(1)
                        .petId(111)
                        .date(date)
                        .description("Visit 1")
                        .build(),
                    Visit.VisitBuilder.aVisit()
                        .id(2)
                        .petId(222)
                        .date(date)
                        .description("Visit 2")
                        .build(),
                    Visit.VisitBuilder.aVisit()
                        .id(3)
                        .petId(222)
                        .date(date)
                        .description("Visit 3")
                        .build()
                )
            );

        mvc.perform(get("/pets/visits?petId=111,222").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.items[1].id").value(2))
            .andExpect(jsonPath("$.items[2].id").value(3))
            .andExpect(jsonPath("$.items[0].petId").value(111))
            .andExpect(jsonPath("$.items[1].petId").value(222))
            .andExpect(jsonPath("$.items[2].petId").value(222))
            .andExpect(jsonPath("$.items[0].description").value("Visit 1"))
            .andExpect(jsonPath("$.items[1].description").value("Visit 2"))
            .andExpect(jsonPath("$.items[2].description").value("Visit 3"));
    }

    @Test 
    void shouldFetchVisitsByPetId() throws Exception {
        Date date = new Date();

        given(visitRepository.findByPetId(123))
            .willReturn(
                asList(
                    Visit.VisitBuilder.aVisit()
                        .id(1)
                        .petId(123)
                        .date(date)
                        .description("Visit 1")
                        .build(),
                    Visit.VisitBuilder.aVisit()
                        .id(12)
                        .petId(123)
                        .date(date)
                        .description("Visit 12")
                        .build()
                )
            );
        
        mvc.perform(get("/owners/2/pets/123/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(12))
            .andExpect(jsonPath("$[0].petId").value(123))
            .andExpect(jsonPath("$[1].petId").value(123))
            .andExpect(jsonPath("$[0].description").value("Visit 1"))
            .andExpect(jsonPath("$[1].description").value("Visit 12"));
    }

    @Test
    void shouldPostAVisit() throws Exception {
        Date date = new Date();

        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(123)
            .description("Visit 1")
            .build();

        given(visitRepository.save(visit))
            .willReturn(visit);

        mvc.perform(post("/owners/2/pets/123/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":1,\"petId\":123,\"description\":\"Visit 1\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.petId").value(123))
            .andExpect(jsonPath("$.description").value("Visit 1"));
    }
}
