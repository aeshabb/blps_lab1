package com.skillbox.enrollment.controller;

import com.skillbox.enrollment.dto.OpenEdxEnrollmentRequest;
import com.skillbox.enrollment.service.OpenEdxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/openedx")
@RequiredArgsConstructor
public class OpenEdxController {

    private final OpenEdxService openEdxService;

    @PostMapping("/register-enroll")
    public ResponseEntity<Map<String, Object>> registerAndEnroll(@RequestBody OpenEdxEnrollmentRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "email is required"));
        }
        if (request.courseId() == null || request.courseId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "courseId is required"));
        }

        boolean success = openEdxService.enrollUser(request.email(), request.courseId());
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "User registered and enrolled in Open edX"));
        }

        return ResponseEntity.status(502).body(Map.of("success", false, "message", "Open edX request failed"));
    }
}
