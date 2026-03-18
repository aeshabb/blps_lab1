package com.skillbox.enrollment.controller;

import com.skillbox.enrollment.dto.ApplicationRequest;
import com.skillbox.enrollment.dto.ApplicationResponse;
import com.skillbox.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(enrollmentService.createApplication(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getApplication(id));
    }
}