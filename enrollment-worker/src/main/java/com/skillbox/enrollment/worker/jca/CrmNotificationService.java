package com.skillbox.enrollment.worker.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Отправляет уведомления в корпоративную CRM-систему через JCA (Jakarta Connectors).
 *
 * Поток:
 *   EnrollmentRequestListener → CrmNotificationService → JCA CCI → HTTP → Mock CRM endpoint
 *
 * JCA обеспечивает стандартизированное подключение к внешней ИС независимо от её реализации.
 */
@Service
@RequiredArgsConstructor
public class CrmNotificationService {

    private static final Logger log = LoggerFactory.getLogger(CrmNotificationService.class);

    private final ConnectionFactory crmConnectionFactory;

    public void notifyEnrollment(String userEmail, String courseId) {
        send(userEmail, courseId, NotificationInteractionSpec.SEND_ENROLLMENT_NOTIFICATION);
    }

    public void notifyEnrollmentFailure(String userEmail, String courseId) {
        send(userEmail, courseId, NotificationInteractionSpec.SEND_FAILURE_NOTIFICATION);
    }

    private void send(String userEmail, String courseId, String operation) {
        try (Connection conn = crmConnectionFactory.getConnection()) {
            var spec = new NotificationInteractionSpec(operation);
            var record = new NotificationRecord(userEmail, courseId, operation);
            try (var interaction = conn.createInteraction()) {
                interaction.execute(spec, record);
            }
            log.info("CRM notified via JCA: operation={}, email={}", operation, userEmail);
        } catch (ResourceException e) {
            log.error("JCA CRM notification failed: {}", e.getMessage());
        }
    }
}
