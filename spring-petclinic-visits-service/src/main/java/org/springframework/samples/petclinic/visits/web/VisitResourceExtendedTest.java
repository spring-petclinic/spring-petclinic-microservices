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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceExtendedTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Test
    void testGetVisitsForPet() throws Exception {
        Visit visit1 = new Visit();
        visit1.setId(1);
        visit1.setDate(new Date());
        visit1.setDescription("Regular checkup");
        visit1.setPetId(7);

        Visit visit2 = new Visit();
        visit2.setId(2);
        visit2.setDate(new Date());
        visit2.setDescription("Vaccination");
        visit2.setPetId(7);

        List<Visit> visits = Arrays.asList(visit1, visit2);
        given(visitRepository.findByPetId(7)).willReturn(visits);

        mvc.perform(get("/owners/*/pets/7/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].description").value("Regular checkup"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].description").value("Vaccination"));
    }

    @Test
    void testGetVisitsForMultiplePets() throws Exception {
        Visit visit1 = new Visit();
        visit1.setId(1);
        visit1.setDate(new Date());
        visit1.setDescription("Regular checkup");
        visit1.setPetId(7);

        Visit visit2 = new Visit();
        visit2.setId(2);
        visit2.setDate(new Date());
        visit2.setDescription("Vaccination");
        visit2.setPetId(8);

        List<Visit> visits = Arrays.asList(visit1, visit2);
        given(visitRepository.findByPetIdIn(Arrays.asList(7, 8))).willReturn(visits);

        mvc.perform(get("/pets/visits")
                .param("petId", "7")
                .param("petId", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items.length()").value(2))
            .andExpect(jsonPath("$.items[0].id").value(1))
            .andExpect(jsonPath("$.items[0].petId").value(7))
            .andExpect(jsonPath("$.items[1].id").value(2))
            .andExpect(jsonPath("$.items[1].petId").value(8));
    }

    @Test
    void testCreateVisit() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(new Date());

        Visit newVisit = new Visit();
        newVisit.setId(1);
        newVisit.setDate(new Date());
        newVisit.setDescription("Dental cleaning");
        newVisit.setPetId(7);

        when(visitRepository.save(any(Visit.class))).thenReturn(newVisit);

        mvc.perform(post("/owners/*/pets/7/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"date\":\"" + formattedDate + "\",\"description\":\"Dental cleaning\"}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Dental cleaning"))
            .andExpect(jsonPath("$.petId").value(7));

        verify(visitRepository).save(any(Visit.class));
    }

    @Test
    void testEmptyVisitsList() throws Exception {
        given(visitRepository.findByPetId(999)).willReturn(List.of());

        mvc.perform(get("/owners/*/pets/999/visits").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }
}