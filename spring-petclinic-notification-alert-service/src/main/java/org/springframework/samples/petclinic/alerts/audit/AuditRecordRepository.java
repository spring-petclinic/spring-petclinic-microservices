package org.springframework.samples.petclinic.alerts.audit;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.Repository;

/**
 * Deliberately a plain Repository: it offers no way to delete a record.
 */
public interface AuditRecordRepository extends Repository<AuditRecord, Long> {

    AuditRecord save(AuditRecord record);

    boolean existsByEventId(UUID eventId);

    List<AuditRecord> findAll(Specification<AuditRecord> specification, Sort sort);

    long count();
}
