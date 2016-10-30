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
package org.springframework.samples.petclinic.boundary.web.vet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.domain.model.vet.Vet;
import org.springframework.samples.petclinic.domain.model.vet.VetService;
import org.springframework.samples.petclinic.support.web.AbstractResourceController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@RestController
public class VetResource extends AbstractResourceController {

    private final VetService vetService;

    @Autowired
    public VetResource(VetService vetService) {
        this.vetService = vetService;
    }

    @GetMapping("/vets")
    public Collection<Vet> showResourcesVetList() {
        return vetService.findVets();
    }
}
