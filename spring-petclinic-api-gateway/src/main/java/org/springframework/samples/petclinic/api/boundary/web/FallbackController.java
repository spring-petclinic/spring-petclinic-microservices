package org.springframework.samples.petclinic.api.boundary.web;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping(value = "/fallback", method = {
        RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<String> fallback() {
        return ResponseEntity.status(HttpStatus.SC_SERVICE_UNAVAILABLE)
                .body("The requested service is currently unavailable. Please try again later.");
    }
}
