package org.springframework.samples.petclinic.genai;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.samples.petclinic.genai.dto.PetRequest;
import org.springframework.samples.petclinic.genai.dto.PetType;
import org.springframework.samples.petclinic.genai.dto.Vet;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIDataProviderTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AIDataProvider aiDataProvider;

    @BeforeEach
    void setUp() {
        aiDataProvider = new AIDataProvider(webClientBuilder);
    }

    @Test
    void testGetAllOwners() {
        // Setup mock owners
        List<OwnerDetails> mockOwners = new ArrayList<>();
        OwnerDetails owner1 = new OwnerDetails(1, "John", "Doe", "123 Main St", "New York", "1234567890", new ArrayList<>());
        OwnerDetails owner2 = new OwnerDetails(2, "Jane", "Smith", "456 Oak St", "Boston", "0987654321", new ArrayList<>());
        mockOwners.add(owner1);
        mockOwners.add(owner2);

        // Setup WebClient mocks
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(java.reactor.core.publisher.Mono.just(mockOwners));

        // Test
        OwnersResponse result = aiDataProvider.getAllOwners();
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.owners().size());
        assertEquals("John", result.owners().get(0).firstName());
        assertEquals("Jane", result.owners().get(1).firstName());
    }

    @Test
    void testAddOwnerToPetclinic() {
        // Setup mock owner
        OwnerDetails mockOwner = new OwnerDetails(1, "John", "Doe", "123 Main St", "New York", "1234567890", new ArrayList<>());
        OwnerRequest request = new OwnerRequest("John", "Doe", "123 Main St", "New York", "1234567890");

        // Setup WebClient mocks
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(java.reactor.core.publisher.Mono.just(mockOwner));

        // Test
        OwnerResponse result = aiDataProvider.addOwnerToPetclinic(request);
        
        // Verify
        assertNotNull(result);
        assertEquals("John", result.owner().firstName());
        assertEquals("Doe", result.owner().lastName());
    }

    @Test
    void testAddPetToOwner() {
        // Setup
        PetRequest petRequest = new PetRequest("Rex", "2020-01-01", 1);
        AddPetRequest request = new AddPetRequest(petRequest, 1);
        
        PetDetails mockPet = new PetDetails(1, "Rex", "2020-01-01", new PetType("dog"), 1, new ArrayList<>());

        // Setup WebClient mocks
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyInt(), anyInt())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(java.reactor.core.publisher.Mono.just(mockPet));

        // Test
        AddedPetResponse result = aiDataProvider.addPetToOwner(request);
        
        // Verify
        assertNotNull(result);
        assertEquals("Rex", result.pet().name());
        assertEquals("dog", result.pet().type().name());
    }

    @Test
    void testGetVets() throws JsonProcessingException {
        // Setup
        Set<org.springframework.samples.petclinic.genai.dto.Specialty> specialties = new HashSet<>();
        specialties.add(new org.springframework.samples.petclinic.genai.dto.Specialty(1, "radiology"));
        Vet mockVet = new Vet(1, "James", "Carter", specialties);
        List<Vet> mockVets = List.of(mockVet);
        VetRequest request = new VetRequest(null);

        // Setup WebClient mocks
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(java.reactor.core.publisher.Mono.just(mockVets));

        // Test
        VetResponse result = aiDataProvider.getVets(request);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.vet().size());
        assertTrue(result.vet().get(0).contains("James Carter"));
        assertTrue(result.vet().get(0).contains("radiology"));
    }
}