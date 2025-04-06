package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OwnerResourceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private OwnerEntityMapper ownerEntityMapper;

    @InjectMocks
    private OwnerResource ownerResource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private void setOwnerId(Owner owner, int id) {
        try {
            Field idField = Owner.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(owner, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCreateOwner() {
        OwnerRequest ownerRequest = new OwnerRequest(
            "John", "Doe", "123 Main St", "Springfield", "1234567890"
        );

        Owner owner = new Owner();
        when(ownerEntityMapper.map(any(Owner.class), eq(ownerRequest))).thenReturn(owner);
        when(ownerRepository.save(owner)).thenReturn(owner);

        Owner createdOwner = ownerResource.createOwner(ownerRequest);

        assertNotNull(createdOwner);
        verify(ownerEntityMapper).map(any(Owner.class), eq(ownerRequest));
        verify(ownerRepository).save(owner);
    }

    @Test
    void testFindOwner() {
        Owner owner = new Owner();
        setOwnerId(owner, 1); // Set the ID using reflection
        when(ownerRepository.findById(1)).thenReturn(Optional.of(owner));

        Optional<Owner> foundOwner = ownerResource.findOwner(1);

        assertTrue(foundOwner.isPresent());
        assertEquals(1, foundOwner.get().getId());
        verify(ownerRepository).findById(1);
    }

    @Test
    void testFindAll() {
        Owner owner1 = new Owner();
        setOwnerId(owner1, 1);
        Owner owner2 = new Owner();
        setOwnerId(owner2, 2);

        when(ownerRepository.findAll()).thenReturn(Arrays.asList(owner1, owner2));

        List<Owner> owners = ownerResource.findAll();

        assertEquals(2, owners.size());
        verify(ownerRepository).findAll();
    }
    @Test
    void testUpdateOwner_Success() {
        int ownerId = 1;
        OwnerRequest ownerRequest = new OwnerRequest(
            "Jane", "Doe", "456 Elm St", "Metropolis", "9876543210"
        );

        Owner existingOwner = new Owner();
        setOwnerId(existingOwner, ownerId);

        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(existingOwner));
        doAnswer(invocation -> {
            Owner owner = invocation.getArgument(0);
            OwnerRequest request = invocation.getArgument(1);
            owner.setFirstName(request.firstName());
            owner.setLastName(request.lastName());
            owner.setAddress(request.address());
            owner.setCity(request.city());
            owner.setTelephone(request.telephone());
            return null;
        }).when(ownerEntityMapper).map(existingOwner, ownerRequest);
        when(ownerRepository.save(existingOwner)).thenReturn(existingOwner);

        ownerResource.updateOwner(ownerId, ownerRequest);

        verify(ownerRepository).findById(ownerId);
        verify(ownerEntityMapper).map(existingOwner, ownerRequest);
        verify(ownerRepository).save(existingOwner);

        assertEquals("Jane", existingOwner.getFirstName());
        assertEquals("Doe", existingOwner.getLastName());
        assertEquals("456 Elm St", existingOwner.getAddress());
        assertEquals("Metropolis", existingOwner.getCity());
        assertEquals("9876543210", existingOwner.getTelephone());
    }

     @Test
    void testUpdateOwner() {
        int ownerId = 1;
        OwnerRequest ownerRequest = new OwnerRequest(
            "Jane", "Doe", "456 Elm St", "Metropolis", "9876543210"
        );
    
        Owner existingOwner = new Owner();
        setOwnerId(existingOwner, ownerId); 
    
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(existingOwner));
        // Mock the mapping behavior
        doAnswer(invocation -> {
            Owner owner = invocation.getArgument(0);
            OwnerRequest request = invocation.getArgument(1);
            owner.setFirstName(request.firstName());
            owner.setLastName(request.lastName());
            owner.setAddress(request.address());
            owner.setCity(request.city());
            owner.setTelephone(request.telephone());
            return null;
        }).when(ownerEntityMapper).map(existingOwner, ownerRequest);
        when(ownerRepository.save(existingOwner)).thenReturn(existingOwner);
    
        ownerResource.updateOwner(ownerId, ownerRequest);
    
        verify(ownerRepository).findById(ownerId);
        verify(ownerEntityMapper).map(existingOwner, ownerRequest);
        verify(ownerRepository).save(existingOwner);
    }

    @Test
    void testUpdateOwner_NotFound() {
        int ownerId = 1;
        OwnerRequest ownerRequest = new OwnerRequest(
            "Jane", "Doe", "456 Elm St", "Metropolis", "9876543210"
        );

        when(ownerRepository.findById(ownerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            ownerResource.updateOwner(ownerId, ownerRequest);
        });

        assertEquals("Owner 1 not found", exception.getMessage());
        verify(ownerRepository).findById(ownerId);
        verifyNoInteractions(ownerEntityMapper);
        verify(ownerRepository, never()).save(any());
    }
}