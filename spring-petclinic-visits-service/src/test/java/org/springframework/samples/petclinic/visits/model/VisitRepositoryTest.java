package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class VisitRepositoryTest {

    @Autowired
    private VisitRepository visitRepository;

    @Test
    void testSaveAndFindVisit() {
        Visit visit = new Visit();
        visit.setDate(new Date());
        visit.setDescription("Annual Checkup");
        visit.setPetId(201);

        Visit savedVisit = visitRepository.save(visit);
        Optional<Visit> foundVisit = visitRepository.findById(savedVisit.getId());

        assertTrue(foundVisit.isPresent());
        assertEquals("Annual Checkup", foundVisit.get().getDescription());
        assertEquals(201, foundVisit.get().getPetId());
    }

    @Test
    void testFindByPetId() {
        Visit visit1 = new Visit();
        visit1.setDate(new Date());
        visit1.setDescription("Dental Cleaning");
        visit1.setPetId(301);

        Visit visit2 = new Visit();
        visit2.setDate(new Date());
        visit2.setDescription("Vaccination");
        visit2.setPetId(301);

        visitRepository.save(visit1);
        visitRepository.save(visit2);

        List<Visit> visits = visitRepository.findByPetId(301);
        assertEquals(2, visits.size());
    }

    @Test
    void testFindByPetIdIn() {
        Visit visit1 = new Visit();
        visit1.setDate(new Date());
        visit1.setDescription("Checkup");
        visit1.setPetId(401);

        Visit visit2 = new Visit();
        visit2.setDate(new Date());
        visit2.setDescription("Surgery");
        visit2.setPetId(402);

        visitRepository.save(visit1);
        visitRepository.save(visit2);

        List<Visit> visits = visitRepository.findByPetIdIn(Arrays.asList(401, 402));
        assertEquals(2, visits.size());
    }

    @Test
    void testDeleteVisit() {
        Visit visit = new Visit();
        visit.setDate(new Date());
        visit.setDescription("Heartworm Prevention");
        visit.setPetId(501);

        Visit savedVisit = visitRepository.save(visit);
        visitRepository.delete(savedVisit);

        Optional<Visit> foundVisit = visitRepository.findById(savedVisit.getId());
        assertFalse(foundVisit.isPresent());
    }
}
