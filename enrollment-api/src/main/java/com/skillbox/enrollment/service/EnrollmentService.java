package com.skillbox.enrollment.service;

import com.skillbox.enrollment.dto.ApplicationRequest;
import com.skillbox.enrollment.dto.ApplicationResponse;
import com.skillbox.enrollment.messaging.EnrollmentRequestMessage;
import com.skillbox.enrollment.messaging.PaymentRequestMessage;
import com.skillbox.enrollment.model.*;
import com.skillbox.enrollment.repository.ApplicationRepository;
import com.skillbox.enrollment.repository.PaymentRepository;
import com.skillbox.enrollment.repository.ProgramRepository;
import com.skillbox.enrollment.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Асинхронный вариант сервиса записи:
 * - createApplication сохраняет заявку и отправляет сообщение в очередь enrollment.payment.request.
 *   enrollment-worker подхватывает его, запрашивает ссылку у банка и сохраняет в Payment.
 * - handlePaymentWebhook обновляет статус платежа и отправляет сообщение в очередь
 *   enrollment.enrollment.request. enrollment-worker зачисляет студента в Open edX.
 */
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final ApplicationRepository applicationRepository;
    private final ProgramRepository programRepository;
    private final TariffRepository tariffRepository;
    private final PaymentRepository paymentRepository;
    private final MessageSenderService messageSender;

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        Program program = programRepository.findById(request.programId())
                .orElseThrow(() -> new IllegalArgumentException("Program not found"));
        Tariff tariff = tariffRepository.findById(request.tariffId())
                .orElseThrow(() -> new IllegalArgumentException("Tariff not found"));

        Application app = new Application();
        app.setProgram(program);
        app.setTariff(tariff);
        app.setUserEmail(request.userEmail());
        app.setUserName(request.userName());
        app.setStatus(ApplicationStatus.CREATED);
        applicationRepository.save(app);

        Payment payment = new Payment();
        payment.setApplication(app);
        payment.setAmount(tariff.getPrice());
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        // Отправляем сообщение воркеру — он запросит ссылку у банка и обновит Payment
        PaymentRequestMessage msg = new PaymentRequestMessage(
                app.getId(), tariff.getPrice(), request.userEmail(), request.userName());
        messageSender.sendPaymentRequest(msg);
        log.info("createApplication: sent PaymentRequestMessage for applicationId={}", app.getId());

        return new ApplicationResponse(app.getId(), app.getStatus().name(), null);
    }

    @Transactional
    public void handlePaymentWebhook(Long applicationId, String paymentStatus) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        Payment payment = paymentRepository.findByApplicationId(applicationId);

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
            payment.setStatus("SUCCESS");
            app.setStatus(ApplicationStatus.PAYMENT_SUCCESS);
            paymentRepository.save(payment);
            applicationRepository.save(app);

            // Отправляем сообщение воркеру — он зачислит студента в Open edX
            EnrollmentRequestMessage msg = new EnrollmentRequestMessage(
                    app.getId(), app.getUserEmail(), app.getProgram().getOpenEdxCourseId());
            messageSender.sendEnrollmentRequest(msg);
            log.info("handlePaymentWebhook: sent EnrollmentRequestMessage for applicationId={}", applicationId);
        } else {
            payment.setStatus("FAILED");
            app.setStatus(ApplicationStatus.PAYMENT_FAILED);
            paymentRepository.save(payment);
            applicationRepository.save(app);
            log.info("handlePaymentWebhook: payment FAILED for applicationId={}", applicationId);
        }
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        Payment payment = paymentRepository.findByApplicationId(id);
        return new ApplicationResponse(
                app.getId(),
                app.getStatus().name(),
                payment != null ? payment.getPaymentLink() : null
        );
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream().map(app -> {
            Payment payment = paymentRepository.findByApplicationId(app.getId());
            return new ApplicationResponse(
                    app.getId(),
                    app.getStatus().name(),
                    payment != null ? payment.getPaymentLink() : null
            );
        }).toList();
    }

    @Transactional
    public ApplicationResponse approveApplication(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        app.setStatus(ApplicationStatus.ENROLLED);
        applicationRepository.save(app);

        Payment payment = paymentRepository.findByApplicationId(id);
        return new ApplicationResponse(
                app.getId(),
                app.getStatus().name(),
                payment != null ? payment.getPaymentLink() : null
        );
    }
}
