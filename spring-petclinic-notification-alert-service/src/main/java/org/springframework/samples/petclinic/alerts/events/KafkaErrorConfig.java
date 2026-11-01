package org.springframework.samples.petclinic.alerts.events;

import java.time.Duration;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {

    /**
     * Retries a failing event, then moves it to the dead-letter topic so that it stops blocking
     * the events behind it. Spring Boot gives this bean to the listener container.
     */
    @Bean
    DefaultErrorHandler visitEventErrorHandler(
        KafkaTemplate<?, ?> kafkaTemplate,
        @Value("${petclinic.events.dead-letter-topic}") String deadLetterTopic,
        @Value("${petclinic.events.retries}") long retries,
        @Value("${petclinic.events.retry-interval}") Duration interval) {

        // The dead-letter topic has a single partition, unlike visit-events
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, failure) -> new TopicPartition(deadLetterTopic, 0));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(interval.toMillis(), retries));
    }
}
