package com.skillbox.enrollment.worker.listener;

import com.skillbox.enrollment.messaging.PaymentRequestMessage;
import com.skillbox.enrollment.model.Application;
import com.skillbox.enrollment.model.ApplicationStatus;
import com.skillbox.enrollment.model.Payment;
import com.skillbox.enrollment.repository.ApplicationRepository;
import com.skillbox.enrollment.repository.PaymentRepository;
import com.skillbox.enrollment.worker.service.BankService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Node 2 — слушает очередь enrollment.payment.request.
 *
 * Получает сообщение от enrollment-api, запрашивает платёжную ссылку у банка
 * и сохраняет её в таблице payments.
 *
 * Транзакционность:
 * - @Transactional + SESSION_TRANSACTED в JmsListenerContainerFactory обеспечивают,
 *   что JMS-сообщение будет подтверждено (acknowledged) только после успешного
 *   коммита транзакции в PostgreSQL.
 * - При ошибке транзакция откатывается, сообщение возвращается в очередь
 *   и будет повторно обработано (at-least-once delivery).
 */
@Component
@RequiredArgsConstructor
public class PaymentRequestListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestListener.class);

    private final BankService bankService;
    private final ApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;

    @JmsListener(destination = "enrollment.payment.request")
    @Transactional
    public void handle(PaymentRequestMessage message) {
        log.info("PaymentRequestListener: received for applicationId={}", message.applicationId());

        Application app = applicationRepository.findById(message.applicationId())
                .orElseThrow(() -> new IllegalStateException("Application not found: " + message.applicationId()));

        String paymentLink = bankService.generatePaymentLink(message.applicationId(), message.amount());

        Payment payment = paymentRepository.findByApplicationId(message.applicationId());
        if (payment == null) {
            throw new IllegalStateException("Payment record not found for application: " + message.applicationId());
        }
        payment.setPaymentLink(paymentLink);
        paymentRepository.save(payment);

        app.setStatus(ApplicationStatus.PAYMENT_PENDING);
        applicationRepository.save(app);

        log.info("PaymentRequestListener: payment link stored for applicationId={}", message.applicationId());
    }
}
