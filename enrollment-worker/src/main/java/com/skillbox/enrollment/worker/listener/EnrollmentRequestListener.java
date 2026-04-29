package com.skillbox.enrollment.worker.listener;

import com.skillbox.enrollment.messaging.EnrollmentRequestMessage;
import com.skillbox.enrollment.model.Application;
import com.skillbox.enrollment.model.ApplicationStatus;
import com.skillbox.enrollment.repository.ApplicationRepository;
import com.skillbox.enrollment.worker.jca.CrmNotificationService;
import com.skillbox.enrollment.worker.service.OpenEdxService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Node 2 — слушает очередь enrollment.enrollment.request.
 *
 * После успешного платежа (подтверждён enrollment-api через webhook):
 *   1. Зачисляет студента в Open edX.
 *   2. Обновляет статус заявки в PostgreSQL (ENROLLED / ENROLLMENT_FAILED).
 *   3. Уведомляет корпоративную CRM-систему через JCA-адаптер.
 *
 * Распределённая транзакция:
 * - @Transactional + SESSION_TRANSACTED: обновление статуса в БД и подтверждение
 *   JMS-сообщения выполняются атомарно. При откате — сообщение вернётся в очередь.
 * - CRM-уведомление выполняется вне транзакции (best-effort): отказ CRM
 *   не должен отменять зачисление студента.
 */
@Component
@RequiredArgsConstructor
public class EnrollmentRequestListener {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentRequestListener.class);

    private final OpenEdxService openEdxService;
    private final ApplicationRepository applicationRepository;
    private final CrmNotificationService crmNotificationService;

    @JmsListener(destination = "enrollment.enrollment.request")
    @Transactional
    public void handle(EnrollmentRequestMessage message) {
        log.info("EnrollmentRequestListener: received for applicationId={}, user={}, course={}",
                message.applicationId(), message.userEmail(), message.courseId());

        Application app = applicationRepository.findById(message.applicationId())
                .orElseThrow(() -> new IllegalStateException("Application not found: " + message.applicationId()));

        boolean enrolled = openEdxService.enrollUser(message.userEmail(), message.courseId());

        if (enrolled) {
            app.setStatus(ApplicationStatus.ENROLLED);
            applicationRepository.save(app);
            log.info("EnrollmentRequestListener: enrolled applicationId={}", message.applicationId());

            // CRM-уведомление через JCA (вне транзакции — best-effort)
            crmNotificationService.notifyEnrollment(message.userEmail(), message.courseId());
        } else {
            app.setStatus(ApplicationStatus.ENROLLMENT_FAILED);
            applicationRepository.save(app);
            log.warn("EnrollmentRequestListener: enrollment failed for applicationId={}", message.applicationId());

            crmNotificationService.notifyEnrollmentFailure(message.userEmail(), message.courseId());
        }
    }
}
