package org.springframework.samples.petclinic.alerts.audit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.springframework.samples.petclinic.alerts.events.VisitEvent;
import org.springframework.samples.petclinic.alerts.events.VisitEventType;
import org.springframework.samples.petclinic.alerts.service.Severity;

/**
 * One permanent record per event. There are no setters and Hibernate ignores changes to it.
 */
@Entity
@Immutable
@Table(name = "audit_record")
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private VisitEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    @Column(name = "visit_id", nullable = false)
    private int visitId;

    @Column(name = "pet_id", nullable = false)
    private int petId;

    @Column(name = "owner_id", nullable = false)
    private int ownerId;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visit_type", nullable = false)
    private String visitType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected AuditRecord() {
    }

    public static AuditRecord of(VisitEvent event, Severity severity, Instant receivedAt) {
        AuditRecord record = new AuditRecord();
        record.eventId = event.eventId();
        record.eventType = event.eventType();
        record.severity = severity;
        record.visitId = event.visitId();
        record.petId = event.petId();
        record.ownerId = event.ownerId();
        record.visitDate = event.visitDate();
        record.visitType = event.visitType();
        record.description = event.description();
        record.occurredAt = event.occurredAt();
        record.receivedAt = receivedAt;
        return record;
    }

    public Long getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public VisitEventType getEventType() {
        return eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getVisitId() {
        return visitId;
    }

    public int getPetId() {
        return petId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public String getVisitType() {
        return visitType;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
