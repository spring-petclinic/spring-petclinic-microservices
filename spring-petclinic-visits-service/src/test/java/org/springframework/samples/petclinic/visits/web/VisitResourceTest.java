package org.springframework.samples.petclinic.visits.web;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.samples.petclinic.visits.service.VisitService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    VisitRepository visitRepository;

    @MockitoBean
    VisitService visitService;

    @Test
    void shouldCreateVisitAndPassTheOwnerIdOn() throws Exception {
        given(visitService.schedule(eq(6), eq(7), any(Visit.class)))
            .willReturn(
                Visit.VisitBuilder.aVisit()
                    .id(42)
                    .petId(7)
                    .date(Date.from(Instant.parse("2026-10-15T00:00:00Z")))
                    .description("Annual check-up")
                    .build()
            );

        mvc.perform(post("/owners/6/pets/7/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-10-15\",\"description\":\"Annual check-up\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.petId").value(7))
            .andExpect(jsonPath("$.date").value("2026-10-15"))
            .andExpect(jsonPath("$.description").value("Annual check-up"));

        verify(visitService).schedule(eq(6), eq(7), any(Visit.class));
    }

    @Test
    void shouldRejectAnOwnerIdBelowOne() throws Exception {
        mvc.perform(post("/owners/0/pets/7/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"2026-10-15\",\"description\":\"Annual check-up\"}"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(visitService);
    }

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
}
