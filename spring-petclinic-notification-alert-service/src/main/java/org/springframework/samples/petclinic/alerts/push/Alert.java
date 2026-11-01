package org.springframework.samples.petclinic.alerts.push;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.samples.petclinic.alerts.audit.AuditRecord;
import org.springframework.samples.petclinic.alerts.events.VisitEventType;
import org.springframework.samples.petclinic.alerts.service.Severity;

/**
 * What an open browser receives on /topic/notifications.
 */
public record Alert(
    UUID eventId,
    VisitEventType eventType,
    Severity severity,
    int visitId,
    int petId,
    int ownerId,
    LocalDate visitDate,
    String visitType,
    String description,
    Instant occurredAt
) {

    static Alert of(AuditRecord record) {
        return new Alert(record.getEventId(), record.getEventType(), record.getSeverity(), record.getVisitId(),
            record.getPetId(), record.getOwnerId(), record.getVisitDate(), record.getVisitType(),
            record.getDescription(), record.getOccurredAt());
    }
}
