/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.clients.boundary.web.owner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.clients.domain.model.owner.Owner;
import org.springframework.samples.petclinic.clients.application.OwnerService;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@RestController
public class OwnerResource {

    private final OwnerService ownerService;

    @Autowired
    public OwnerResource(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    private Owner retrieveOwner(int ownerId) {
        return this.ownerService.findOwnerById(ownerId);
    }

    /**
     * Create Owner
     */
    @RequestMapping(value = "/owner", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createOwner(@Valid @RequestBody Owner owner) {
        this.ownerService.saveOwner(owner);
    }

    /**
     * Read single Owner
     */
    @RequestMapping(value = "/owner/{ownerId}", method = RequestMethod.GET)
    public Owner findOwner(@PathVariable("ownerId") int ownerId) {
        return retrieveOwner(ownerId);
    }

    /**
     * Read List of Owners
     */
    @GetMapping("/owner/list")
    public Collection<Owner> findAll() {
        return ownerService.findAll();
    }

    /**
     * Update Owner
     */
    @RequestMapping(value = "/owner/{ownerId}", method = RequestMethod.PUT)
    public Owner updateOwner(@PathVariable("ownerId") int ownerId, @Valid @RequestBody Owner ownerRequest) {
        Owner ownerModel = retrieveOwner(ownerId);
        // This is done by hand for simplicity purpose. In a real life use-case we should consider using MapStruct.
        ownerModel.setFirstName(ownerRequest.getFirstName());
        ownerModel.setLastName(ownerRequest.getLastName());
        ownerModel.setCity(ownerRequest.getCity());
        ownerModel.setAddress(ownerRequest.getAddress());
        ownerModel.setTelephone(ownerRequest.getTelephone());
        this.ownerService.saveOwner(ownerModel);
        return ownerModel;
    }


}
