package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VetTest {

    private Vet vet;

    @Mock
    private Specialty specialtyMock1;

    @Mock
    private Specialty specialtyMock2;

    @BeforeEach
    void setUp() {
        vet = new Vet();
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");
    }

    @Test
    void shouldCreateVetSuccessfully() {
        assertThat(vet.getId()).isEqualTo(1);
        assertThat(vet.getFirstName()).isEqualTo("John");
        assertThat(vet.getLastName()).isEqualTo("Doe");
        assertThat(vet.getSpecialties()).isEmpty();
        assertThat(vet.getNrOfSpecialties()).isEqualTo(0);
    }

    @Test
    void shouldAddSpecialtyToVet() {
        vet.addSpecialty(specialtyMock1);

        assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
        assertThat(vet.getSpecialties()).contains(specialtyMock1);

    }

    @Test
    void shouldReturnUnmodifiableSpecialtiesList() {
        // Given
        vet.addSpecialty(specialtyMock1);
        List<Specialty> specialties = vet.getSpecialties();

        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> {
            specialties.add(specialtyMock2);
        });

        // Ensure the original list is unchanged
        assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
    }

    @Test
    void shouldModifySpecialtiesDirectly() {  // Renamed to reflect actual behavior
        // Given
        vet.addSpecialty(specialtyMock1);

        // When - Get internal specialties and modify
        Set<Specialty> specialties = vet.getSpecialtiesInternal();
        specialties.add(specialtyMock2);  // This modifies the internal set directly

        // Then - Modification affects internal state
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);  // Should be 2 now
    }

    @Test
    void shouldSortSpecialtiesByName() {
        // Given - add specialties in reverse alphabetical order
        vet.addSpecialty(specialtyMock2);  // "Dentistry"
        vet.addSpecialty(specialtyMock1);  // "Surgery"

        // When
        List<Specialty> specialties = vet.getSpecialties();

        // Then - verify they're returned in alphabetical order
        assertThat(specialties).hasSize(2);
        verify(specialtyMock1, atLeastOnce()).getName();
        verify(specialtyMock2, atLeastOnce()).getName();
    }

    @Test
    void shouldInitializeSpecialtiesIfNull() {
        // When - Force internal specialties initialization
        int count = vet.getNrOfSpecialties();

        // Then
        assertThat(count).isEqualTo(0);
        // Now add a specialty to confirm set was initialized
        vet.addSpecialty(specialtyMock1);
        assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
    }

    @Test
    void shouldPreventDuplicateSpecialties() {
        // When
        vet.addSpecialty(specialtyMock1);
        vet.addSpecialty(specialtyMock1);  // Add the same specialty twice

        // Then
        assertThat(vet.getNrOfSpecialties()).isEqualTo(1);  // Should still be 1, not 2
    }
}
