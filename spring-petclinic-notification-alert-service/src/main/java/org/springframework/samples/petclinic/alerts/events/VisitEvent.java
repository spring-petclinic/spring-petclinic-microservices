package org.springframework.samples.petclinic.alerts.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * The event read from Kafka, as defined by {@code contracts/visit-event-schema.json}.
 * A missing field fails here, which sends the message to the dead-letter topic.
 */
public record VisitEvent(
    UUID eventId,
    VisitEventType eventType,
    Integer visitId,
    Integer petId,
    Integer ownerId,
    LocalDate visitDate,
    String visitType,
    String description,
    Instant occurredAt
) {

    public VisitEvent {
        Objects.requireNonNull(eventId, "eventId");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(visitId, "visitId");
        Objects.requireNonNull(petId, "petId");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(visitDate, "visitDate");
        Objects.requireNonNull(visitType, "visitType");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
