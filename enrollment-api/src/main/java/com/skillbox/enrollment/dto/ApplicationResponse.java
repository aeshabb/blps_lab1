package com.skillbox.enrollment.dto;

public record ApplicationResponse(
    Long id,
    String status,
    String paymentLink
) {}