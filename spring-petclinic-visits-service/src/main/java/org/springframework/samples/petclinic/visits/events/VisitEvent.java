package org.springframework.samples.petclinic.visits.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
     * Visits have no type yet, so every event carries this one until the visit type is added.
     */
    public static final String DEFAULT_VISIT_TYPE = "ROUTINE";

    /**
     * Builds the event for a visit that has just been saved.
     *
     * @param visit      the saved visit, with its id and date set
     * @param ownerId    owner of the pet, taken from the request path
     * @param occurredAt when the visit was booked
     */
    public static VisitEvent scheduled(Visit visit, int ownerId, Instant occurredAt) {
        return new VisitEvent(
            UUID.randomUUID(),
            VisitEventType.VISIT_SCHEDULED,
            visit.getId(),
            visit.getPetId(),
            ownerId,
            // the visit date is parsed as midnight UTC, so read it back in UTC
            visit.getDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate(),
            DEFAULT_VISIT_TYPE,
            visit.getDescription() == null ? "" : visit.getDescription(),
            occurredAt.truncatedTo(ChronoUnit.MILLIS)
        );
    }
}
