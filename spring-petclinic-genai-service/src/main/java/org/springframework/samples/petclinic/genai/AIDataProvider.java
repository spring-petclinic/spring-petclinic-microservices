package org.springframework.samples.petclinic.genai;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.samples.petclinic.genai.dto.PetRequest;
import org.springframework.samples.petclinic.genai.dto.Vet;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Functions that are invoked by the LLM will use this bean to query the system of record
 * for information such as listing owners and vets, or adding pets to an owner.
 *
 * @author Oded Shopen
 */
@Service
public class AIDataProvider {

	private final VectorStore vectorStore;

    private final RestClient restClient;

    private final String customersServiceUrl;

	public AIDataProvider(VectorStore vectorStore,
                          @Value("${petclinic.customers-service.url}") String customersServiceUrl) {
        this.restClient = RestClient.builder().build();
        this.vectorStore = vectorStore;
        this.customersServiceUrl = customersServiceUrl;
    }

	public List<OwnerDetails> getAllOwners() {
        return restClient
            .get()
            .uri(customersServiceUrl + "/owners")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });
	}

    public List<String> getVets(Vet vetRequest) throws JacksonException {
		ObjectMapper objectMapper = new ObjectMapper();
		String vetAsJson = objectMapper.writeValueAsString(vetRequest);

        int topK = 20;
        if (vetRequest == null) {
            // Provide a limit of 50 results when zero parameters are sent
            topK = 50;
        }
        SearchRequest sr = SearchRequest.builder()
            .query(vetAsJson)
            .topK(topK)
            .build();


		List<Document> topMatches = this.vectorStore.similaritySearch(sr);
		return topMatches.stream().map(Document::getFormattedContent).toList();
	}

	public PetDetails addPetToOwner(int ownerId, PetRequest petRequest) {
        return restClient
            .post()
            .uri(customersServiceUrl + "/owners/" + ownerId + "/pets")
            .body(petRequest)
            .retrieve()
            .body(PetDetails.class);
	}

	public OwnerDetails addOwnerToPetclinic(OwnerRequest ownerRequest) {
       return restClient
            .post()
            .uri(customersServiceUrl + "/owners")
            .body(ownerRequest)
            .retrieve()
            .body(OwnerDetails.class);
	}

}
