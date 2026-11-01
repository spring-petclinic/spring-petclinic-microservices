package org.springframework.samples.petclinic.visits.service;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.petclinic.visits.events.VisitEvent;
import org.springframework.samples.petclinic.visits.events.VisitEventType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class VisitServiceTest {

    private VisitRepository visitRepository;

    private ApplicationEventPublisher events;

    private VisitService visitService;

    @BeforeEach
    void setUp() {
        visitRepository = mock(VisitRepository.class);
        events = mock(ApplicationEventPublisher.class);
        visitService = new VisitService(visitRepository, events);
        // the database assigns the id on save
        given(visitRepository.save(any(Visit.class))).willAnswer(call -> {
            Visit visit = call.getArgument(0);
            visit.setId(42);
            return visit;
        });
    }

    @Test
    void savesTheVisitForThePetInThePath() {
        Visit visit = Visit.VisitBuilder.aVisit().date(LocalDate.now()).description("check-up").build();

        Visit saved = visitService.schedule(6, 7, visit);

        assertThat(saved.getId()).isEqualTo(42);
        assertThat(saved.getPetId()).isEqualTo(7);
        verify(visitRepository).save(visit);
    }

    @Test
    void announcesExactlyOneScheduledEventWithTheOwnerId() {
        Visit visit = Visit.VisitBuilder.aVisit()
            .date(LocalDate.parse("2026-10-15"))
            .description("check-up")
            .visitType("EMERGENCY")
            .build();

        visitService.schedule(6, 7, visit);

        ArgumentCaptor<VisitEvent> event = ArgumentCaptor.forClass(VisitEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().eventType()).isEqualTo(VisitEventType.VISIT_SCHEDULED);
        assertThat(event.getValue().visitId()).isEqualTo(42);
        assertThat(event.getValue().petId()).isEqualTo(7);
        assertThat(event.getValue().ownerId()).isEqualTo(6);
        assertThat(event.getValue().visitDate()).isEqualTo(LocalDate.parse("2026-10-15"));
        assertThat(event.getValue().visitType()).isEqualTo("EMERGENCY");
        assertThat(event.getValue().eventId()).isNotNull();
    }

    @Test
    void savesButAnnouncesNothingWhenTheVisitHasNoDate() {
        Visit visit = Visit.VisitBuilder.aVisit().description("no date").build();

        Visit saved = visitService.schedule(6, 7, visit);

        assertThat(saved.getId()).isEqualTo(42);
        verify(events, never()).publishEvent(any());
    }
}
