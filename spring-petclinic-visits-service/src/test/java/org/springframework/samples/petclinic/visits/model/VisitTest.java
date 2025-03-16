package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VisitTest {

    @Test
    void testVisitBuilder() {
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .petId(123)
            .description("Test visit")
            .date(new Date())
            .build();

        assertEquals(1, visit.getId());
        assertEquals(123, visit.getPetId());
        assertEquals("Test visit", visit.getDescription());
        assertNotNull(visit.getDate());
    }

    @Test
    void testVisitSettersAndGetters() {
        Visit visit = new Visit();
        visit.setId(2);
        visit.setPetId(456);
        visit.setDescription("Another visit");
        Date date = new Date();
        visit.setDate(date);

        assertEquals(2, visit.getId());
        assertEquals(456, visit.getPetId());
        assertEquals("Another visit", visit.getDescription());
        assertEquals(date, visit.getDate());
    }
}
