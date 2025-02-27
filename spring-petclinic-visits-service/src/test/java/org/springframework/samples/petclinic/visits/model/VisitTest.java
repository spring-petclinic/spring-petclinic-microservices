package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class VisitTest {

    @Test
    void testVisitSettersAndGetters() {
        Visit visit = new Visit();
        visit.setId(1);
        visit.setDate(new Date());
        visit.setDescription("Regular Checkup");
        visit.setPetId(101);

        assertEquals(1, visit.getId());
        assertNotNull(visit.getDate());
        assertEquals("Regular Checkup", visit.getDescription());
        assertEquals(101, visit.getPetId());
    }

    @Test
    void testVisitBuilder() {
        Date visitDate = new Date();
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(2)
            .date(visitDate)
            .description("Vaccination")
            .petId(102)
            .build();

        assertEquals(2, visit.getId());
        assertEquals(visitDate, visit.getDate());
        assertEquals("Vaccination", visit.getDescription());
        assertEquals(102, visit.getPetId());
    }
}
