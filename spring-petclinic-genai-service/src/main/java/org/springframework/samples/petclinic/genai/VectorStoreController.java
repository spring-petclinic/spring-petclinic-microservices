package org.springframework.samples.petclinic.genai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.samples.petclinic.genai.dto.Vet;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

/**
 * Loads the veterinarians data into a vector store for the purpose of RAG functionality.
 *
 * @author Oded Shopen
 */
@Component
public class VectorStoreController {

	private final Logger logger = LoggerFactory.getLogger(VectorStoreController.class);

	private final VectorStore vectorStore;
    private final WebClient webClient;

    public VectorStoreController(VectorStore vectorStore, WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.build();
		this.vectorStore = vectorStore;
	}

	@EventListener
	public void loadVetDataToVectorStoreOnStartup(ApplicationStartedEvent event) throws IOException {
		Resource resource = new ClassPathResource("vectorstore.json");

		// Check if file exists
		if (resource.exists()) {
			// In order to save on AI credits, use a pre-embedded database that was saved
			// to
			// disk based on the current data in the h2 data.sql file
			File file = resource.getFile();
			((SimpleVectorStore) this.vectorStore).load(file);
			logger.info("vector store loaded from existing vectorstore.json file in the classpath");
			return;
		}

		// If vectorstore.json is deleted, the data will be loaded on startup every time.
		// Warning - this can be costly in terms of credits used with the AI provider.
		// Fetches all Vet entites and creates a document per vet
        String vetsHostname = "http://vets-service/";
        List<Vet> vets = webClient
	            .get()
	            .uri(vetsHostname + "vets")
	            .retrieve()
	            .bodyToMono(new ParameterizedTypeReference<List<Vet>>() {})
	            .block();


		Resource vetsAsJson = convertListToJsonResource(vets);
		DocumentReader reader = new JsonReader(vetsAsJson);

		List<Document> documents = reader.get();
		// add the documents to the vector store
		this.vectorStore.add(documents);

		if (vectorStore instanceof SimpleVectorStore) {
            // java:S5443 Sonar rule: Using publicly writable directories is security-sensitive
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
            File file = Files.createTempFile("vectorstore", ".json", attr).toFile();
			((SimpleVectorStore) this.vectorStore).save(file);
			logger.info("vector store contents written to {}", file.getAbsolutePath());
		}

		logger.info("vector store loaded with {} documents", documents.size());
	}

	public Resource convertListToJsonResource(List<Vet> vets) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			// Convert List<Vet> to JSON string
			String json = objectMapper.writeValueAsString(vets);

			// Convert JSON string to byte array
			byte[] jsonBytes = json.getBytes();

			// Create a ByteArrayResource from the byte array
			return new ByteArrayResource(jsonBytes);
		}
		catch (JsonProcessingException e) {
            logger.error("Error processing JSON in the convertListToJsonResource function", e);
			return null;
		}
	}

}
