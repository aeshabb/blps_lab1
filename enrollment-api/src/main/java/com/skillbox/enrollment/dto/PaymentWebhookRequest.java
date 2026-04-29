package com.skillbox.enrollment.dto;

public record PaymentWebhookRequest(
    Long applicationId,
    String status // "SUCCESS" or "FAIL"
) {}