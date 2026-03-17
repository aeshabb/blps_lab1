package com.skillbox.enrollment.controller;

import com.skillbox.enrollment.dto.PaymentWebhookRequest;
import com.skillbox.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> paymentWebhook(@RequestBody PaymentWebhookRequest request) {
        enrollmentService.handlePaymentWebhook(request.applicationId(), request.status());
        return ResponseEntity.ok().build();
    }
}