package org.springframework.samples.petclinic.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

class ApiGatewayApplicationRouterTest {

    private final WebTestClient client = WebTestClient.bindToRouterFunction(
        new ApiGatewayApplication().routerFunction()).build();

    @Test
    void servesTheVueApplicationForHtmlNavigation() {
        client.get()
            .uri("/owners/details/3")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
            .expectBody(String.class).value(body -> body.contains("<div id=\"app\"></div>"));
    }

    @Test
    void doesNotServeTheVueApplicationForApiRequests() {
        client.get()
            .uri("/api/customer/owners")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void doesNotServeTheVueApplicationForWebjars() {
        client.get()
            .uri("/webjars/missing.js")
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void doesNotServeTheVueApplicationForScriptRequests() {
        client.get()
            .uri("/scripts/main.js")
            .accept(MediaType.ALL)
            .exchange()
            .expectStatus().isNotFound();
    }
}
