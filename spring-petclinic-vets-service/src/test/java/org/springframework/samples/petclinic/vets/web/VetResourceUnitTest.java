// src/test/java/org/springframework/samples/petclinic/vets/web/VetResourceUnitTest.java
package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for VetResource using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class VetResourceUnitTest {

    @Mock
    VetRepository vetRepository;

    @InjectMocks
    VetResource vetResource;

    private Vet vet1;

    @BeforeEach
    void setUp() {
        vet1 = new Vet();
        vet1.setId(1);
        vet1.setFirstName("James");
        vet1.setLastName("Carter");
    }

    @Test
    void showResourcesVetList_ShouldReturnVetsFromRepository() {
        // Arrange
        List<Vet> expectedVets = Collections.singletonList(vet1);
        when(vetRepository.findAll()).thenReturn(expectedVets);

        // Act
        List<Vet> actualVets = vetResource.showResourcesVetList();

        // Assert
        assertNotNull(actualVets, "Returned list should not be null");
        assertEquals(expectedVets.size(), actualVets.size(), "Returned list size should match expected");
        assertEquals(expectedVets.get(0), actualVets.get(0), "Returned list content should match expected");

        // Verify interaction
        verify(vetRepository, times(1)).findAll();
        verifyNoMoreInteractions(vetRepository); // Ensure no other methods were called
    }

    @Test
    void showResourcesVetList_ShouldReturnEmptyListWhenNoVets() {
        // Arrange
        List<Vet> expectedVets = Collections.emptyList();
        when(vetRepository.findAll()).thenReturn(expectedVets);

        // Act
        List<Vet> actualVets = vetResource.showResourcesVetList();

        // Assert
        assertNotNull(actualVets, "Returned list should not be null");
        assertTrue(actualVets.isEmpty(), "Returned list should be empty");

        // Verify interaction
        verify(vetRepository, times(1)).findAll();
        verifyNoMoreInteractions(vetRepository);
    }

    // Note: The @Cacheable annotation is not tested in this pure unit test.
    // Cache behavior testing often requires Spring context or specific cache testing libraries.
    // The existing WebMvcTest might cover some integration aspects including caching if configured.
}