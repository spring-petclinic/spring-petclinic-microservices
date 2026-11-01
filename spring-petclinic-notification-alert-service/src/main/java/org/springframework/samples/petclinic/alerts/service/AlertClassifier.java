package org.springframework.samples.petclinic.alerts.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Decides how urgent a visit is. Which types are urgent is configuration, so the list
 * can change without a code change; a type that is not on it, known or not, is routine.
 */
@Component
public class AlertClassifier {

    private final Set<String> urgentTypes;

    public AlertClassifier(@Value("${petclinic.alerts.urgent-types}") Set<String> urgentTypes) {
        this.urgentTypes = urgentTypes;
    }

    public Severity classify(String visitType) {
        return urgentTypes.contains(visitType) ? Severity.EMERGENCY : Severity.ROUTINE;
    }
}
