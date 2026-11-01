package org.springframework.samples.petclinic.visits.events;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.visits.model.Visit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the JSON this service puts on Kafka against the shared contract,
 * {@code contracts/visit-event-schema.json} in the repository root.
 */
class VisitEventContractTest {

    // Maven runs tests from the module directory
    private static final Path SCHEMA_FILE = Path.of("..", "contracts", "visit-event-schema.json");

    private static JsonSchema schema;

    @BeforeAll
    static void loadSchema() throws IOException {
        // Formats (uuid, date, date-time) are only annotations unless assertion is switched on
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build();
        schema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
            .getSchema(Files.readString(SCHEMA_FILE), config);
    }

    @Test
    void scheduledEventMatchesTheContract() {
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(12)
            .petId(7)
            .date(LocalDate.parse("2026-10-15"))
            .description("Annual check-up")
            .build();

        String json = VisitEventPublisher.toJson(
            VisitEvent.scheduled(visit, 6, Instant.parse("2026-10-01T14:32:05.123456Z")));

        assertThat(validate(json)).as(json).isEmpty();
        assertThat(json)
            .contains("\"eventType\":\"VISIT_SCHEDULED\"")
            .contains("\"visitId\":12")
            .contains("\"petId\":7")
            .contains("\"ownerId\":6")
            .contains("\"visitDate\":\"2026-10-15\"")
            .contains("\"visitType\":\"ROUTINE\"")
            .contains("\"occurredAt\":\"2026-10-01T14:32:05.123Z\"");
    }

    @Test
    void eventWithoutDescriptionMatchesTheContract() {
        Visit visit = Visit.VisitBuilder.aVisit()
            .id(13)
            .petId(2)
            .date(LocalDate.parse("2026-10-02"))
            .visitType("URGENT_CARE")
            .build();

        String json = VisitEventPublisher.toJson(VisitEvent.scheduled(visit, 2, Instant.now()));

        assertThat(validate(json)).as(json).isEmpty();
        assertThat(json).contains("\"description\":\"\"").contains("\"visitType\":\"URGENT_CARE\"");
    }

    @Test
    void validatorRejectsWhatTheContractForbids() {
        // Guards the test itself: a validator with format checking off would accept these
        String valid = VisitEventPublisher.toJson(VisitEvent.scheduled(
            Visit.VisitBuilder.aVisit().id(1).petId(1).date(LocalDate.parse("2026-10-15")).build(),
            1, Instant.parse("2026-10-01T14:32:05Z")));

        assertThat(validate(valid)).isEmpty();
        assertThat(validate(valid.replace("2026-10-15", "2026-02-30"))).as("impossible date").isNotEmpty();
        assertThat(validate(valid.replace("\"ownerId\":1", "\"ownerId\":0"))).as("owner id 0").isNotEmpty();
        assertThat(validate(valid.replace("VISIT_SCHEDULED", "VISIT_MOVED"))).as("unknown type").isNotEmpty();
    }

    private static Set<ValidationMessage> validate(String json) {
        return schema.validate(json, InputFormat.JSON);
    }
}
