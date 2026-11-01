package org.springframework.samples.petclinic.alerts.push;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.samples.petclinic.alerts.audit.AuditRecord;
import org.springframework.samples.petclinic.alerts.events.VisitEvent;
import org.springframework.samples.petclinic.alerts.events.VisitEventType;
import org.springframework.samples.petclinic.alerts.service.Severity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AlertPushServiceTest {

    private final SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);

    private final AlertPushService pushService = new AlertPushService(messaging);

    private final AuditRecord record = AuditRecord.of(
        new VisitEvent(UUID.randomUUID(), VisitEventType.VISIT_SCHEDULED, 1, 7, 6, LocalDate.parse("2026-11-01"),
            "URGENT_CARE", "limping", Instant.parse("2026-10-31T10:00:00Z")),
        Severity.EMERGENCY, Instant.now());

    @Test
    void sendsTheClassifiedAlertToTheTopic() {
        pushService.push(record);

        ArgumentCaptor<Object> alert = ArgumentCaptor.forClass(Object.class);
        verify(messaging).convertAndSend(eq("/topic/notifications"), alert.capture());
        assertThat(alert.getValue()).isInstanceOfSatisfying(Alert.class, sent -> {
            assertThat(sent.severity()).isEqualTo(Severity.EMERGENCY);
            assertThat(sent.petId()).isEqualTo(7);
            assertThat(sent.visitType()).isEqualTo("URGENT_CARE");
        });
    }

    @Test
    void aFailedPushDoesNotFailTheEvent() {
        willThrow(new MessagingException("broker unavailable")).given(messaging)
            .convertAndSend(eq("/topic/notifications"), any(Object.class));

        assertThatCode(() -> pushService.push(record)).doesNotThrowAnyException();
    }
}
