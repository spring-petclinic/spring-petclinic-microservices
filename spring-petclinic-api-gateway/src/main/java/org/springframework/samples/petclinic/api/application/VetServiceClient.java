package org.springframework.samples.petclinic.api.application;

import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.VetDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class VetServiceClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<VetDetails> getVet(final int vetId) {
        return webClientBuilder.build().get()
            .uri("http://vets-service/vets/{vetId}", vetId)
            .retrieve()
            .bodyToMono(VetDetails.class);
    }
}
