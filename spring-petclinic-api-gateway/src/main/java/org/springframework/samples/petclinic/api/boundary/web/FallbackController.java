package org.springframework.samples.petclinic.api.boundary.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    /**
     * Fallback for the CircuitBreaker filter, which is applied as a default filter to
     * every route. The filter forwards the original request here unchanged, so this
     * handler must accept every method: restricting it to POST turns an open circuit
     * on a GET route into a misleading 405 instead of a 503.
     */
    @RequestMapping("/fallback")
    public ResponseEntity<String> fallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Service is currently unavailable. Please try again later.");
    }
}
