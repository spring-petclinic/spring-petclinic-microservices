package org.springframework.samples.petclinic.api.boundary.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

class FallbackControllerTest {

    private final WebTestClient client = WebTestClient.bindToController(new FallbackController()).build();

    @Test
    void returnsServiceUnavailableForGetRequests() {
        client.get()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody(String.class).isEqualTo("The requested service is currently unavailable. Please try again later.");
    }

    @Test
    void returnsServiceUnavailableForPostRequests() {
        client.post()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void returnsServiceUnavailableForPutRequests() {
        client.put()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void returnsServiceUnavailableForDeleteRequests() {
        client.delete()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void returnsServiceUnavailableForPatchRequests() {
        client.patch()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void returnsCustomFallbackMessageWhenConfigured() {
        String customMessage = "PetClinic is currently under maintenance. Please try again shortly.";
        WebTestClient customClient = WebTestClient.bindToController(new FallbackController(customMessage)).build();

        customClient.get()
            .uri("/fallback")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody(String.class).isEqualTo(customMessage);
    }
}
