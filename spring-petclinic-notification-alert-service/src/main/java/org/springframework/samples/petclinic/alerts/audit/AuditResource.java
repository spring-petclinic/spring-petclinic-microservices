package org.springframework.samples.petclinic.alerts.audit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AuditResource {

    private final AuditRecordRepository auditRecords;

    AuditResource(AuditRecordRepository auditRecords) {
        this.auditRecords = auditRecords;
    }

    /**
     * Searches audit records, newest first. Every filter is optional.
     *
     * @param type visit type, for example EMERGENCY
     * @param from first visit date to include
     * @param to   last visit date to include
     */
    @GetMapping("audit")
    List<AuditRecord> search(
        @RequestParam(required = false) Integer petId,
        @RequestParam(required = false) Integer ownerId,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Specification<AuditRecord> filters = (record, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (petId != null) {
                predicates.add(builder.equal(record.get("petId"), petId));
            }
            if (ownerId != null) {
                predicates.add(builder.equal(record.get("ownerId"), ownerId));
            }
            if (type != null) {
                predicates.add(builder.equal(record.get("visitType"), type));
            }
            if (from != null) {
                predicates.add(builder.greaterThanOrEqualTo(record.get("visitDate"), from));
            }
            if (to != null) {
                predicates.add(builder.lessThanOrEqualTo(record.get("visitDate"), to));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        return auditRecords.findAll(filters, Sort.by(Sort.Direction.DESC, "occurredAt"));
    }
}
