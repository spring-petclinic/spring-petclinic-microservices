package org.springframework.samples.petclinic.visits.events;

/**
 * What happened to a visit. The names are the values of {@code eventType} in the VisitEvent contract.
 */
public enum VisitEventType {
    VISIT_SCHEDULED,
    VISIT_CANCELLED
}
