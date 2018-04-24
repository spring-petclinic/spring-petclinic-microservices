package org.springframework.samples.petclinic.api.application;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lain
 */
@FeignClient("visits-service")
public interface VisitsServiceFeignClient {

    @GetMapping("/owners/{ownerId}/pets/{petId}/visits")
    List<VisitDetails> getVisitDetails(@PathVariable("ownerId") final int ownerId, @PathVariable("petId") final int petId);
}
