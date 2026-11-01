package org.springframework.samples.petclinic.visits.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visits.events.VisitEvent;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.samples.petclinic.visits.model.VisitStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VisitService {

    private static final Logger log = LoggerFactory.getLogger(VisitService.class);

    private final VisitRepository visitRepository;

    private final ApplicationEventPublisher events;

    public VisitService(VisitRepository visitRepository, ApplicationEventPublisher events) {
        this.visitRepository = visitRepository;
        this.events = events;
    }

    /**
     * Saves the visit and announces it. The announcement is only sent to Kafka
     * once this transaction has committed (see VisitEventPublisher).
     */
    @Transactional
    public Visit schedule(int ownerId, int petId, Visit visit) {
        visit.setPetId(petId);
        log.info("Saving visit {}", visit);
        Visit saved = visitRepository.save(visit);
        if (saved.getDate() == null) {
            // the contract requires a visit date, so there is no valid event to send
            log.warn("Visit {} has no date; no event published", saved.getId());
        } else {
            events.publishEvent(VisitEvent.scheduled(saved, ownerId, Instant.now()));
        }
        return saved;
    }

    /**
     * Marks the visit cancelled; the row is kept.
     *
     * @throws ResponseStatusException 404 if the pet has no such visit, 409 if it is already cancelled
     */
    @Transactional
    public Visit cancel(int ownerId, int petId, int visitId) {
        Visit visit = visitRepository.findById(visitId)
            .filter(found -> found.getPetId() == petId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Pet " + petId + " has no visit " + visitId));
        if (visit.getStatus() == VisitStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visit " + visitId + " is already cancelled");
        }
        visit.cancel();
        log.info("Cancelling visit {}", visitId);
        Visit saved = visitRepository.save(visit);
        if (saved.getDate() == null) {
            log.warn("Visit {} has no date; no event published", saved.getId());
        } else {
            events.publishEvent(VisitEvent.cancelled(saved, ownerId, Instant.now()));
        }
        return saved;
    }
}
