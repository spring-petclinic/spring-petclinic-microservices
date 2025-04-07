package org.springframework.samples.petclinic.customers.web.Mapper;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OwnerEntityMapperTest {

    @Test
    void shouldMapOwnerRequestToOwner() {
        // Given
        OwnerRequest ownerRequest = new OwnerRequest(
            "John",
            "Doe",
            "123 Main St",
            "Anytown",
            "555-1234"
        );

        Owner owner = new Owner();

        // When
        OwnerEntityMapper mapper = new OwnerEntityMapper();
        Owner mappedOwner = mapper.map(owner, ownerRequest);

        // Then
        assertEquals("John", mappedOwner.getFirstName());
        assertEquals("Doe", mappedOwner.getLastName());
        assertEquals("123 Main St", mappedOwner.getAddress());
        assertEquals("Anytown", mappedOwner.getCity());
        assertEquals("555-1234", mappedOwner.getTelephone());
    }
}