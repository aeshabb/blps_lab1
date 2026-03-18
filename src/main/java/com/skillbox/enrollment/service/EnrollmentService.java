package com.skillbox.enrollment.service;

import com.skillbox.enrollment.dto.ApplicationRequest;
import com.skillbox.enrollment.dto.ApplicationResponse;
import com.skillbox.enrollment.model.*;
import com.skillbox.enrollment.repository.ApplicationRepository;
import com.skillbox.enrollment.repository.PaymentRepository;
import com.skillbox.enrollment.repository.ProgramRepository;
import com.skillbox.enrollment.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final ApplicationRepository applicationRepository;
    private final ProgramRepository programRepository;
    private final TariffRepository tariffRepository;
    private final PaymentRepository paymentRepository;
    private final OpenEdxService openEdxService;
    private final BankService bankService;

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
        app.setStatus(ApplicationStatus.PAYMENT_PENDING);

        applicationRepository.save(app);

        Payment payment = new Payment();
        payment.setApplication(app);
        payment.setAmount(tariff.getPrice());
        payment.setStatus("PENDING");
        
        // Receive payment link from the bank service
        String paymentLink = bankService.generatePaymentLink(app.getId(), tariff.getPrice());
        
        payment.setPaymentLink(paymentLink);
        paymentRepository.save(payment);

        return new ApplicationResponse(app.getId(), app.getStatus().name(), paymentLink);
    }

    @Transactional
    public void handlePaymentWebhook(Long applicationId, String paymentStatus) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        Payment payment = paymentRepository.findByApplicationId(applicationId);

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) {
            payment.setStatus("SUCCESS");
            app.setStatus(ApplicationStatus.PAYMENT_SUCCESS);
            
            // Trigger Open EdX enrollment
            boolean enrolled = openEdxService.enrollUser(app.getUserEmail(), app.getProgram().getOpenEdxCourseId());
            if (enrolled) {
                app.setStatus(ApplicationStatus.ENROLLED);
            } else {
                app.setStatus(ApplicationStatus.ENROLLMENT_FAILED);
            }
        } else {
            payment.setStatus("FAILED");
            app.setStatus(ApplicationStatus.PAYMENT_FAILED);
        }

        paymentRepository.save(payment);
        applicationRepository.save(app);
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
}