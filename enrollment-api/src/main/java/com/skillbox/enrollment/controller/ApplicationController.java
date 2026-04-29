package com.skillbox.enrollment.controller;

import com.skillbox.enrollment.dto.ApplicationRequest;
import com.skillbox.enrollment.dto.ApplicationResponse;
import com.skillbox.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(enrollmentService.getAllApplications());
    }

    /**
     * Создаёт заявку и асинхронно инициирует запрос платёжной ссылки.
     * Возвращает 202 Accepted: paymentLink будет null, пока enrollment-worker
     * не получит ссылку от банка. Клиент опрашивает GET /{id} до появления paymentLink.
     */
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@RequestBody ApplicationRequest request) {
        return ResponseEntity.accepted().body(enrollmentService.createApplication(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getApplication(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApplicationResponse> approveApplication(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.approveApplication(id));
    }
}
