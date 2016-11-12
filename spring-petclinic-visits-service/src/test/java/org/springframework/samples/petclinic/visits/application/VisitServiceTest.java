package org.springframework.samples.petclinic.visits.application;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.samples.petclinic.visits.VisitsServiceApplication;
import org.springframework.samples.petclinic.visits.domain.model.visit.Visit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = VisitsServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
public class VisitServiceTest {

    @Autowired
    protected VisitService visitService;

    @Test
    @Transactional
    public void shouldAddNewVisitForPet() {
        // given
        int petId = 7;
        Visit visit = new Visit();
        visit.setPetId(petId);
        visit.setDescription("test");
        //when
        visitService.saveVisit(visit);
        // then
        assertThat(visit.getPetId()).isEqualTo(petId);
    }

}
