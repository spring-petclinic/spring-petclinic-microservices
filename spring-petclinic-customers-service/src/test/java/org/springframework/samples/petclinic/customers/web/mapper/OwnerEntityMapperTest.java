package org.springframework.samples.petclinic.customers.web.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;
import static org.junit.jupiter.api.Assertions.*;

class OwnerEntityMapperTest {

    private OwnerEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OwnerEntityMapper();
    }

    @Test
    void testMapOwnerRequestToOwner() {
        OwnerRequest request = new OwnerRequest("Alice", "Smith", "456 Elm St", "Los Angeles", "9876543210");
        Owner owner = new Owner();

        Owner mappedOwner = mapper.map(owner, request);

        assertEquals("Alice", mappedOwner.getFirstName());
        assertEquals("Smith", mappedOwner.getLastName());
        assertEquals("456 Elm St", mappedOwner.getAddress());
        assertEquals("Los Angeles", mappedOwner.getCity());
        assertEquals("9876543210", mappedOwner.getTelephone());
    }
}
