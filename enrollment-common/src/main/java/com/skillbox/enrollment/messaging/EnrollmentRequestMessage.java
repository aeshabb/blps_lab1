package com.skillbox.enrollment.messaging;

import java.io.Serializable;

/**
 * Отправляется из enrollment-api в очередь enrollment.enrollment.request
 * после успешного подтверждения платежа банком.
 * enrollment-worker получает и зачисляет студента в Open edX,
 * затем отправляет уведомление в CRM через JCA.
 */
public record EnrollmentRequestMessage(
        Long applicationId,
        String userEmail,
        String courseId
) implements Serializable {}
