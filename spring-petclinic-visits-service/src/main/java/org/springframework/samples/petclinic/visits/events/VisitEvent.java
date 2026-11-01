package org.springframework.samples.petclinic.visits.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.samples.petclinic.visits.model.Visit;

/**
 * Event published to Kafka when a visit is booked or cancelled.
 * The field names and formats are fixed by {@code contracts/visit-event-schema.json}.
 */
public record VisitEvent(
    UUID eventId,
    VisitEventType eventType,
    int visitId,
    int petId,
    int ownerId,
    LocalDate visitDate,
    String visitType,
    String description,
    Instant occurredAt
) {

    /**
     * Builds the event for a visit that has just been saved.
     *
     * @param visit      the saved visit, with its id and date set
     * @param ownerId    owner of the pet, taken from the request path
     * @param occurredAt when the visit was booked
     */
    public static VisitEvent scheduled(Visit visit, int ownerId, Instant occurredAt) {
        return of(VisitEventType.VISIT_SCHEDULED, visit, ownerId, occurredAt);
    }

    /**
     * Builds the event for a visit that has just been cancelled.
     */
    public static VisitEvent cancelled(Visit visit, int ownerId, Instant occurredAt) {
        return of(VisitEventType.VISIT_CANCELLED, visit, ownerId, occurredAt);
    }

    private static VisitEvent of(VisitEventType type, Visit visit, int ownerId, Instant occurredAt) {
        return new VisitEvent(
            UUID.randomUUID(),
            type,
            visit.getId(),
            visit.getPetId(),
            ownerId,
            visit.getDate(),
            visit.getVisitType(),
            visit.getDescription() == null ? "" : visit.getDescription(),
            occurredAt.truncatedTo(ChronoUnit.MILLIS)
        );
    }
}
