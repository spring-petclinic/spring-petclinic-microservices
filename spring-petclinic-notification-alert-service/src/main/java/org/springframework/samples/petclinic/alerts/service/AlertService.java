package org.springframework.samples.petclinic.alerts.service;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.alerts.audit.AuditRecord;
import org.springframework.samples.petclinic.alerts.audit.AuditRecordRepository;
import org.springframework.samples.petclinic.alerts.events.VisitEvent;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AuditRecordRepository auditRecords;

    private final AlertClassifier classifier;

    public AlertService(AuditRecordRepository auditRecords, AlertClassifier classifier) {
        this.auditRecords = auditRecords;
        this.classifier = classifier;
    }

    /**
     * Classifies the event and stores one audit record. Kafka may deliver an event more than
     * once, so an event id that is already stored is skipped.
     *
     * @return the new record, or empty if this event was already processed
     */
    public Optional<AuditRecord> handle(VisitEvent event) {
        if (auditRecords.existsByEventId(event.eventId())) {
            log.info("Skipping event {}: already processed", event.eventId());
            return Optional.empty();
        }
        Severity severity = classifier.classify(event.visitType());
        try {
            AuditRecord saved = auditRecords.save(AuditRecord.of(event, severity, Instant.now()));
            log.info("Stored {} {} as {} for visit {}", event.eventType(), event.eventId(), severity, event.visitId());
            return Optional.of(saved);
        } catch (DataIntegrityViolationException duplicate) {
            // Another instance stored the same event between the check and the insert
            log.info("Skipping event {}: stored concurrently", event.eventId());
            return Optional.empty();
        }
    }
}
