package com.skillbox.enrollment.scheduler;

import com.skillbox.enrollment.model.Application;
import com.skillbox.enrollment.model.ApplicationStatus;
import com.skillbox.enrollment.model.Payment;
import com.skillbox.enrollment.repository.ApplicationRepository;
import com.skillbox.enrollment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quartz-задание: каждый час находит заявки со статусом PAYMENT_PENDING,
 * которые старше 24 часов, и переводит их в PAYMENT_FAILED.
 * Гарантирует, что "зависшие" заявки не блокируют ресурсы системы.
 */
@Component
@RequiredArgsConstructor
public class ExpireStalePaymentsJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(ExpireStalePaymentsJob.class);
    private static final long EXPIRE_AFTER_HOURS = 24;

    private final ApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(EXPIRE_AFTER_HOURS);
        List<Application> stale = applicationRepository
                .findByStatusAndCreatedAtBefore(ApplicationStatus.PAYMENT_PENDING, threshold);

        if (stale.isEmpty()) {
            log.info("ExpireStalePayments: no stale applications found");
            return;
        }

        log.info("ExpireStalePayments: expiring {} stale application(s)", stale.size());

        for (Application app : stale) {
            app.setStatus(ApplicationStatus.PAYMENT_FAILED);
            applicationRepository.save(app);

            Payment payment = paymentRepository.findByApplicationId(app.getId());
            if (payment != null && "PENDING".equals(payment.getStatus())) {
                payment.setStatus("EXPIRED");
                paymentRepository.save(payment);
            }

            log.info("Expired application id={}, email={}", app.getId(), app.getUserEmail());
        }
    }
}
