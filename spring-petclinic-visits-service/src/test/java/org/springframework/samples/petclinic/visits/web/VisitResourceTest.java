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
import static org.mockito.Mockito.verify;
import static java.util.Arrays.asList;
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
    void shouldReturnEmptyListWhenNoVisitsFound() throws Exception {
        // Given
        given(visitRepository.findByPetIdIn(asList(999))).willReturn(List.of());

        // When/Then
        mvc.perform(get("/pets/visits?petId=999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isEmpty()); // Expect an empty array
    }

    @Test
    void shouldFetchVisitsForSinglePet() throws Exception {
        // Given
        given(visitRepository.findByPetIdIn(asList(123)))
            .willReturn(
                asList(
                    Visit.VisitBuilder.aVisit()
                        .id(5)
                        .petId(123)
                        .build()
                )
            );

        // When/Then
        mvc.perform(get("/pets/visits?petId=123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].id").value(5))
            .andExpect(jsonPath("$.items[0].petId").value(123));
    }

    @Test
    void shouldHandleInvalidPetIdFormat() throws Exception {
        // When/Then
        mvc.perform(get("/pets/visits?petId=invalid"))
            .andExpect(status().isBadRequest()); // Expect HTTP 400 Bad Request
    }

    @Test
    void shouldVerifyVisitRepositoryCalled() throws Exception {
        // Given
        given(visitRepository.findByPetIdIn(asList(321))).willReturn(List.of());

        // When
        mvc.perform(get("/pets/visits?petId=321"))
            .andExpect(status().isOk());

        // Then - Verify that the repository method was called
        verify(visitRepository).findByPetIdIn(asList(321));
    }

    @Test
    void shouldCreateVisitSuccessfully() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit().id(10).petId(123).build();
        given(visitRepository.save(visit)).willReturn(visit);

        mvc.perform(post("/owners/*/pets/123/visits")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": 10, \"petId\": 123}"))
            .andExpect(status().isCreated());
    }
}
