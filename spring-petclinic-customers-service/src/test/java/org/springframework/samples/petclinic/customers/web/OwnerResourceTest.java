package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerResourceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private OwnerEntityMapper ownerEntityMapper;

    @InjectMocks
    private OwnerResource ownerResource;

    private Owner owner;
    private OwnerRequest ownerRequest;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Main St");
        owner.setCity("Springfield");
        owner.setTelephone("1234567890");

        ownerRequest = new OwnerRequest(
            "John",
            "Doe",
            "123 Main St",
            "Springfield",
            "1234567890"
        );
    }

    @Test
    void testCreateOwner() {
        // Arrange
        when(ownerEntityMapper.map(any(Owner.class), eq(ownerRequest))).thenReturn(owner);
        when(ownerRepository.save(owner)).thenReturn(owner);

        // Act
        Owner createdOwner = ownerResource.createOwner(ownerRequest);

        // Assert
        assertThat(createdOwner).isNotNull();
        assertThat(createdOwner.getFirstName()).isEqualTo("John");
        assertThat(createdOwner.getLastName()).isEqualTo("Doe");
        verify(ownerRepository).save(any(Owner.class));
        verify(ownerEntityMapper).map(any(Owner.class), eq(ownerRequest));
    }

    @Test
    void testFindOwner() {
        // Arrange
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        // Act
        Optional<Owner> foundOwner = ownerResource.findOwner(1);

        // Assert
        assertThat(foundOwner).isPresent();
        assertThat(foundOwner.get().getFirstName()).isEqualTo("John");
        assertThat(foundOwner.get().getLastName()).isEqualTo("Doe");
        verify(ownerRepository).findById(1);
    }

    @Test
    void testFindAll() {
        // Arrange
        Owner owner2 = new Owner();
        owner2.setFirstName("Jane");
        owner2.setLastName("Smith");

        List<Owner> owners = Arrays.asList(owner, owner2);
        when(ownerRepository.findAll()).thenReturn(owners);

        // Act
        List<Owner> foundOwners = ownerResource.findAll();

        // Assert
        assertThat(foundOwners).hasSize(2);
        assertThat(foundOwners.get(0).getFirstName()).isEqualTo("John");
        assertThat(foundOwners.get(1).getFirstName()).isEqualTo("Jane");
        verify(ownerRepository).findAll();
    }

    @Test
    void testUpdateOwner() {
        // Arrange
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        // Act
        ownerResource.updateOwner(1, ownerRequest);

        // Assert
        verify(ownerRepository).findById(1);
        verify(ownerEntityMapper).map(eq(owner), eq(ownerRequest));
        verify(ownerRepository).save(owner);
    }

    @Test
    void testUpdateOwnerNotFound() {
        // Arrange
        when(ownerRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            ownerResource.updateOwner(999, ownerRequest);
        });

        verify(ownerRepository).findById(999);
        verify(ownerEntityMapper, never()).map(any(Owner.class), any(OwnerRequest.class));
        verify(ownerRepository, never()).save(any(Owner.class));
    }
}
