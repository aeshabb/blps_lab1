package com.skillbox.enrollment.dto;

public record OpenEdxEnrollmentRequest(
        String email,
        String courseId
) {
}
