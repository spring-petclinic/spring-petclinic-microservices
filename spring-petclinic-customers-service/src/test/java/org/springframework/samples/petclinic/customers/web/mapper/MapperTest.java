package org.springframework.samples.petclinic.customers.web.mapper;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;

class MapperTest {

    @Test
    void testMapperInterface() {
        Mapper<OwnerRequest, Owner> mapper = (response, request) -> {
            response.setFirstName(request.firstName());
            return response;
        };

        Owner owner = new Owner();
        OwnerRequest request = new OwnerRequest("John", "Doe", "123 Main St", "New York", "1234567890");

        Owner mappedOwner = mapper.map(owner, request);

        assertEquals("John", mappedOwner.getFirstName());
    }
}
