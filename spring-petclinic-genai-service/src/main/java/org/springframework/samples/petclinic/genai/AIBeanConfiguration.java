package org.springframework.samples.petclinic.genai;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * A Configuration class for beans used by the Chat Client.
 *
 * @author Oded Shopen
 */
@Configuration
public class AIBeanConfiguration {

	@Bean
	VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}

    /**
     * Downstream services are reached through their Kubernetes Service DNS names,
     * so no client-side load balancing is needed: the Service load-balances across pods.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
