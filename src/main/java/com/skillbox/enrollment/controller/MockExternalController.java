package com.skillbox.enrollment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mock")
public class MockExternalController {

    private static final Logger log = LoggerFactory.getLogger(MockExternalController.class);

    @PostMapping("/bank/generate-link")
    public ResponseEntity<Map<String, String>> generateBankLink(@RequestBody Map<String, Object> request) {
        Object applicationId = request.get("applicationId");
        Object amount = request.get("amount");
        log.info("Mock Bank: Generating payment link for application {}, amount {}", applicationId, amount);

        String paymentLink = "/payment-gateway.html?applicationId=" + applicationId;
        return ResponseEntity.ok(Map.of("paymentLink", paymentLink));
    }

    @PostMapping("/openedx/enroll")
    public ResponseEntity<Void> enrollInOpenEdx(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String courseId = request.get("courseId");
        log.info("Mock Open edX: Successfully enrolled user {} in course {}", email, courseId);
        
        return ResponseEntity.ok().build();
    }
}