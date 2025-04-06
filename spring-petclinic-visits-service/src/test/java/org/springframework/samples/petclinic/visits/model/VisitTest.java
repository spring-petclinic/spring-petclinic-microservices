package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class VisitTest {

    private Visit visit;

    @BeforeEach
    public void setUp() {
        visit = Visit.VisitBuilder.aVisit()
            .id(1)
            .date(new Date())
            .description("Regular check-up")
            .petId(101)
            .build();
    }

    @Test
    void shouldCreateVisitWithValidData() {
        // Assert that the visit object is not null
        assertThat(visit).isNotNull();

        // Assert that the visit ID is set correctly
        assertThat(visit.getId()).isEqualTo(1);

        // Assert that the visit date is set correctly
        assertThat(visit.getDate()).isNotNull();

        // Assert that the description is set correctly
        assertThat(visit.getDescription()).isEqualTo("Regular check-up");

        // Assert that the petId is set correctly
        assertThat(visit.getPetId()).isEqualTo(101);
    }

    @Test
    void shouldSetVisitDescription() {
        // Set a new description
        visit.setDescription("Annual vaccination");

        // Assert that the description is updated
        assertThat(visit.getDescription()).isEqualTo("Annual vaccination");
    }

    @Test
    void shouldSetVisitDate() {
        Date newDate = new Date(1672531199000L); // Some date for testing

        // Set a new date
        visit.setDate(newDate);

        // Assert that the date is updated
        assertThat(visit.getDate()).isEqualTo(newDate);
    }

    @Test
    void shouldSetPetId() {
        // Set a new petId
        visit.setPetId(202);

        // Assert that the petId is updated
        assertThat(visit.getPetId()).isEqualTo(202);
    }

    @Test
    void shouldCreateVisitUsingBuilder() {
        Visit newVisit = Visit.VisitBuilder.aVisit()
            .id(2)
            .date(new Date())
            .description("Dental check")
            .petId(102)
            .build();

        // Assert that the new visit object is created successfully with the builder
        assertThat(newVisit).isNotNull();
        assertThat(newVisit.getId()).isEqualTo(2);
        assertThat(newVisit.getDescription()).isEqualTo("Dental check");
        assertThat(newVisit.getPetId()).isEqualTo(102);
    }
}
