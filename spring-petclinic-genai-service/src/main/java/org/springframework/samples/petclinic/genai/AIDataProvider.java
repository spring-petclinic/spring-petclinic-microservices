package org.springframework.samples.petclinic.genai;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Functions that are invoked by the LLM will use this bean to query the system of record
 * for information such as listing owners and vets, or adding pets to an owner.
 *
 * @author Oded Shopen
 */
@Service
public class AIDataProvider {

	private final VectorStore vectorStore;
    private final String ownersHostname = "http://customers-service/";

    private final WebClient webClient;


	public AIDataProvider(WebClient.Builder webClientBuilder, VectorStore vectorStore) {
		this.webClient = webClientBuilder.build();
		this.vectorStore = vectorStore;
	}

	public OwnersResponse getAllOwners() {
		return new OwnersResponse(webClient
	            .get()
	            .uri(ownersHostname + "owners")
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<List<OwnerDetails>>() {})
	            .block());
	}

	public VetResponse getVets(VetRequest request) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		String vetAsJson = objectMapper.writeValueAsString(request.vet());

		SearchRequest sr = SearchRequest.from(SearchRequest.defaults()).withQuery(vetAsJson).withTopK(20);
		if (request.vet() == null) {
			// Provide a limit of 50 results when zero parameters are sent
			sr = sr.withTopK(50);
		}

		List<Document> topMatches = this.vectorStore.similaritySearch(sr);
		List<String> results = topMatches.stream().map(Document::getContent).toList();
		return new VetResponse(results);
	}

	public AddedPetResponse addPetToOwner(AddPetRequest request) {
		return new AddedPetResponse(webClient
	            .post()
	            .uri(ownersHostname + "owners/"+request.ownerId()+"/pets")
	            .bodyValue(request.pet())
	            .retrieve().bodyToMono(PetDetails.class).block());
	}

	public OwnerResponse addOwnerToPetclinic(OwnerRequest ownerRequest) {
		return new OwnerResponse(webClient
	            .post()
	            .uri(ownersHostname + "owners")
	            .bodyValue(ownerRequest)
	            .retrieve().bodyToMono(OwnerDetails.class).block());
	}

}
