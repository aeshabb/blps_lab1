package com.skillbox.enrollment.dto;

public record ApplicationRequest(
    Long programId,
    Long tariffId,
    String userEmail,
    String userName
) {}