package org.springframework.samples.petclinic.api.boundary.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class FallbackController {

    private final String fallbackMessage;

    public FallbackController() {
        this("The requested service is currently unavailable. Please try again later.");
    }

    public FallbackController(
            @Value("${petclinic.fallback.message:The requested service is currently unavailable. Please try again later.}")
            String fallbackMessage) {
        this.fallbackMessage = fallbackMessage;
    }

    @GetMapping("/fallback")
    public ResponseEntity<String> fallback() {
        return fallbackResponse();
    }

    @PostMapping("/fallback")
    public ResponseEntity<String> fallbackPost() {
        return fallbackResponse();
    }

    @PutMapping("/fallback")
    public ResponseEntity<String> fallbackPut() {
        return fallbackResponse();
    }

    @DeleteMapping("/fallback")
    public ResponseEntity<String> fallbackDelete() {
        return fallbackResponse();
    }

    @PatchMapping("/fallback")
    public ResponseEntity<String> fallbackPatch() {
        return fallbackResponse();
    }

    private ResponseEntity<String> fallbackResponse() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(fallbackMessage);
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }
}
