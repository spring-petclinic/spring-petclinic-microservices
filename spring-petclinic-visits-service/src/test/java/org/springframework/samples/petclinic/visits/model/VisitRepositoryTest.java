package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VisitRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisitRepository visitRepository;

    @Test
    void shouldSaveVisit() {
        // Given
        Visit visit = Visit.VisitBuilder.aVisit()
            .petId(1)
            .date(new Date())
            .description("First visit")
            .build();

        // When
        Visit saved = visitRepository.save(visit);

        // Then
        assertNotNull(saved.getId());
        assertEquals(visit.getPetId(), saved.getPetId());
        assertEquals(visit.getDescription(), saved.getDescription());
    }

    @Test
    void shouldFindVisitsByPetId() {
        // Given
        Visit visit1 = createAndPersistVisit(1, "First visit");
        Visit visit2 = createAndPersistVisit(1, "Second visit");
        createAndPersistVisit(2, "Other pet visit"); // Different pet

        // When
        List<Visit> visits = visitRepository.findByPetId(1);

        // Then
        assertEquals(2, visits.size());
        assertTrue(visits.stream().anyMatch(v -> v.getId().equals(visit1.getId())));
        assertTrue(visits.stream().anyMatch(v -> v.getId().equals(visit2.getId())));
    }

    @Test
    void shouldFindVisitsByPetIds() {
        // Given
        Visit visit1 = createAndPersistVisit(1, "Pet 1 visit");
        Visit visit2 = createAndPersistVisit(2, "Pet 2 visit");
        createAndPersistVisit(3, "Pet 3 visit"); // Not in search

        // When
        List<Visit> visits = visitRepository.findByPetIdIn(Arrays.asList(1, 2));

        // Then
        assertEquals(2, visits.size());
        assertTrue(visits.stream().anyMatch(v -> v.getId().equals(visit1.getId())));
        assertTrue(visits.stream().anyMatch(v -> v.getId().equals(visit2.getId())));
    }

    @Test
    void shouldReturnEmptyListWhenNoPetIdsProvided() {
        // Given
        createAndPersistVisit(1, "Test visit");

        // When
        List<Visit> visits = visitRepository.findByPetIdIn(List.of());

        // Then
        assertTrue(visits.isEmpty());
    }

    private Visit createAndPersistVisit(int petId, String description) {
        Visit visit = Visit.VisitBuilder.aVisit()
            .petId(petId)
            .date(new Date())
            .description(description)
            .build();
        return entityManager.persist(visit);
    }
} 