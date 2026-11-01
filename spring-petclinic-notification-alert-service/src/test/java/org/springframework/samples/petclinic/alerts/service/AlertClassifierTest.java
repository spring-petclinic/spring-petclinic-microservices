package org.springframework.samples.petclinic.alerts.service;

import java.util.Set;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertClassifierTest {

    private final AlertClassifier classifier = new AlertClassifier(Set.of("EMERGENCY", "URGENT_CARE"));

    @ParameterizedTest
    @CsvSource({
        "EMERGENCY, EMERGENCY",
        "URGENT_CARE, EMERGENCY",
        "ROUTINE, ROUTINE",
        "VACCINATION, ROUTINE",
        "SOMETHING_UNKNOWN, ROUTINE"
    })
    void classifiesByTheConfiguredList(String visitType, Severity expected) {
        assertThat(classifier.classify(visitType)).isEqualTo(expected);
    }

    @Test
    void theListIsConfiguration() {
        AlertClassifier changed = new AlertClassifier(Set.of("SURGERY"));

        assertThat(changed.classify("SURGERY")).isEqualTo(Severity.EMERGENCY);
        assertThat(changed.classify("EMERGENCY")).isEqualTo(Severity.ROUTINE);
    }
}
