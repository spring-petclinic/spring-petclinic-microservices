package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VisitTest {

    @Test
    void shouldCreateVisitWithBuilder() {
        // Given
        Date visitDate = new Date();
        String description = "Regular checkup";
        int petId = 1;

        // When
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .date(visitDate)
            .description(description)
            .petId(petId)
            .build();

        // Then
        assertEquals(1, visit.getId());
        assertEquals(visitDate, visit.getDate());
        assertEquals(description, visit.getDescription());
        assertEquals(petId, visit.getPetId());
    }

    @Test
    void shouldCreateVisitWithSetters() {
        // Given
        Visit visit = new Visit();
        Date visitDate = new Date();

        // When
        visit.setId(1);
        visit.setDate(visitDate);
        visit.setDescription("Annual vaccination");
        visit.setPetId(2);

        // Then
        assertEquals(1, visit.getId());
        assertEquals(visitDate, visit.getDate());
        assertEquals("Annual vaccination", visit.getDescription());
        assertEquals(2, visit.getPetId());
    }

    @Test
    void shouldHaveDefaultDateWhenCreated() {
        // When
        Visit visit = new Visit();

        // Then
        assertNotNull(visit.getDate());
    }

    @Test
    void shouldAllowNullDescription() {
        // Given
        Visit visit = new Visit();

        // When
        visit.setDescription(null);

        // Then
        assertNull(visit.getDescription());
    }
} 