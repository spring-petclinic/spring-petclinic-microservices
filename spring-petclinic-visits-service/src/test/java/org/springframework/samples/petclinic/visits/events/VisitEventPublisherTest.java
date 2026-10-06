package org.springframework.samples.petclinic.visits.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VisitEventPublisherTest {

    private static final String TOPIC = "visit-events";

    private static final VisitEvent EVENT = new VisitEvent(
        UUID.fromString("3f2b8c1e-9a4d-4e6f-8b7a-1c2d3e4f5a6b"),
        VisitEventType.VISIT_SCHEDULED,
        12, 7, 6,
        LocalDate.parse("2026-10-15"),
        "ROUTINE",
        "Annual check-up",
        Instant.parse("2026-10-01T14:32:05Z"));

    private KafkaTemplate<String, String> kafkaTemplate;

    private VisitEventPublisher publisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new VisitEventPublisher(kafkaTemplate, TOPIC);
    }

    @Test
    void sendsTheEventAsJsonKeyedByPetId() {
        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .willReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        publisher.publish(EVENT);

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(TOPIC), eq("7"), payload.capture());
        assertThat(payload.getValue())
            .contains("\"eventId\":\"3f2b8c1e-9a4d-4e6f-8b7a-1c2d3e4f5a6b\"")
            .contains("\"eventType\":\"VISIT_SCHEDULED\"")
            .contains("\"ownerId\":6")
            .contains("\"occurredAt\":\"2026-10-01T14:32:05Z\"");
    }

    @Test
    void doesNotFailWhenTheSendIsRejectedLater() {
        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .willReturn(CompletableFuture.failedFuture(new TimeoutException("broker not available")));

        assertThatCode(() -> publisher.publish(EVENT)).doesNotThrowAnyException();
    }

    @Test
    void doesNotFailWhenTheSendThrows() {
        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .willThrow(new KafkaException("no metadata"));

        assertThatCode(() -> publisher.publish(EVENT)).doesNotThrowAnyException();
    }
}
