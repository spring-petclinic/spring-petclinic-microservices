package org.springframework.samples.petclinic.api.application;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomersServiceClientIntegrationTest {

    private static final Integer OWNER_ID = 1;

    private TestCustomersServiceClient customersServiceClient;

    private MockWebServer server;

    @BeforeEach
    void setUp() {
        server = new MockWebServer();
        WebClient.Builder webClientBuilder = WebClient.builder();
        customersServiceClient = new TestCustomersServiceClient(webClientBuilder, server.url("/").toString());
    }

    @AfterEach
    void shutdown() throws IOException {
        this.server.shutdown();
    }

    @Test
    void getOwner_withAvailableCustomersService() {
        // Prepare mock response
        prepareResponse(response -> response
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"Boston\",\"telephone\":\"1234567890\",\"pets\":[{\"id\":1,\"name\":\"Max\",\"birthDate\":\"2018-09-07\",\"type\":{\"id\":1,\"name\":\"dog\"}}]}"));

        // Call the service
        Mono<OwnerDetails> ownerDetailsMono = customersServiceClient.getOwner(OWNER_ID);
        OwnerDetails ownerDetails = ownerDetailsMono.block();

        // Verify the response
        assertOwnerDetailsEquals(ownerDetails, OWNER_ID, "John", "Doe", "123 Main St", "Boston", "1234567890");
        assertEquals(1, ownerDetails.pets().size());
        assertEquals(1, ownerDetails.pets().get(0).id());
        assertEquals("Max", ownerDetails.pets().get(0).name());
    }

    @Test
    void getOwner_shouldReturnPetIds() {
        // Prepare mock response with multiple pets
        prepareResponse(response -> response
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":1,\"firstName\":\"John\",\"lastName\":\"Doe\",\"address\":\"123 Main St\",\"city\":\"Boston\",\"telephone\":\"1234567890\",\"pets\":[{\"id\":1,\"name\":\"Max\",\"birthDate\":\"2018-09-07\",\"type\":{\"id\":1,\"name\":\"dog\"}},{\"id\":2,\"name\":\"Bella\",\"birthDate\":\"2019-03-15\",\"type\":{\"id\":1,\"name\":\"dog\"}}]}"));

        // Call the service
        Mono<OwnerDetails> ownerDetailsMono = customersServiceClient.getOwner(OWNER_ID);
        OwnerDetails ownerDetails = ownerDetailsMono.block();

        // Verify the pet IDs
        assertEquals(2, ownerDetails.getPetIds().size());
        assertEquals(1, ownerDetails.getPetIds().get(0));
        assertEquals(2, ownerDetails.getPetIds().get(1));
    }

    private void assertOwnerDetailsEquals(OwnerDetails ownerDetails, int id, String firstName, String lastName,
                                          String address, String city, String telephone) {
        assertNotNull(ownerDetails);
        assertEquals(id, ownerDetails.id());
        assertEquals(firstName, ownerDetails.firstName());
        assertEquals(lastName, ownerDetails.lastName());
        assertEquals(address, ownerDetails.address());
        assertEquals(city, ownerDetails.city());
        assertEquals(telephone, ownerDetails.telephone());
    }

    private void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        this.server.enqueue(response);
    }

    /**
     * Test version of CustomersServiceClient that uses a configurable base URL
     */
    static class TestCustomersServiceClient extends CustomersServiceClient {
        private final String baseUrl;
        private final WebClient.Builder webClientBuilder;

        public TestCustomersServiceClient(WebClient.Builder webClientBuilder, String baseUrl) {
            super(webClientBuilder);
            this.webClientBuilder = webClientBuilder;
            this.baseUrl = baseUrl;
        }

        @Override
        public Mono<OwnerDetails> getOwner(final int ownerId) {
            return webClientBuilder.build().get()
                .uri(baseUrl + "owners/{ownerId}", ownerId)
                .retrieve()
                .bodyToMono(OwnerDetails.class);
        }
    }
}
