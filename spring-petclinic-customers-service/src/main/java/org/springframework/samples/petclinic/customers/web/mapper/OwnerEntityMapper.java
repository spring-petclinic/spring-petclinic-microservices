package org.springframework.samples.petclinic.customers.web.mapper;

import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.web.OwnerRequest;
import org.springframework.stereotype.Component;

@Component
public class OwnerEntityMapper implements Mapper<OwnerRequest, Owner> {
    @Override
    public Owner map(final Owner owner, final OwnerRequest request) {
        owner.setAddress(request.address());
        owner.setCity(request.city());
        owner.setTelephone(request.telephone());
        owner.setFirstName(request.firstName());
        owner.setLastName(request.lastName());
        return owner;
    }
}
