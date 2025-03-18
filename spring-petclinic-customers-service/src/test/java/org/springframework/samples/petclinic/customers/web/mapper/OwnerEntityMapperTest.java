package org.springframework.samples.petclinic.customers.web.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;

import static org.junit.jupiter.api.Assertions.*;

class OwnerEntityMapperTest {

    private final OwnerEntityMapper mapper = new OwnerEntityMapper();

    @Test
    void shouldMapOwnerRequestToOwner() {
        // Given
        Owner owner = new Owner();
        OwnerRequest request = new OwnerRequest(
            "John",
            "Doe",
            "123 Main St",
            "New York",
            "1234567890"
        );

        // When
        Owner mappedOwner = mapper.map(owner, request);

        // Then
        assertEquals(request.firstName(), mappedOwner.getFirstName());
        assertEquals(request.lastName(), mappedOwner.getLastName());
        assertEquals(request.address(), mappedOwner.getAddress());
        assertEquals(request.city(), mappedOwner.getCity());
        assertEquals(request.telephone(), mappedOwner.getTelephone());
    }

    @Test
    void shouldUpdateExistingOwner() {
        // Given
        Owner owner = new Owner();
        owner.setFirstName("Old First");
        owner.setLastName("Old Last");
        owner.setAddress("Old Address");
        owner.setCity("Old City");
        owner.setTelephone("0000000000");

        OwnerRequest request = new OwnerRequest(
            "New First",
            "New Last",
            "New Address",
            "New City",
            "1234567890"
        );

        // When
        Owner mappedOwner = mapper.map(owner, request);

        // Then
        assertEquals(request.firstName(), mappedOwner.getFirstName());
        assertEquals(request.lastName(), mappedOwner.getLastName());
        assertEquals(request.address(), mappedOwner.getAddress());
        assertEquals(request.city(), mappedOwner.getCity());
        assertEquals(request.telephone(), mappedOwner.getTelephone());
    }
} 