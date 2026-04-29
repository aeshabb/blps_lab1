package com.skillbox.enrollment.service;

import com.skillbox.enrollment.messaging.EnrollmentRequestMessage;
import com.skillbox.enrollment.messaging.PaymentRequestMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * Отправляет сообщения в RabbitMQ через AMQP 1.0 (Apache Qpid JMS).
 * Каждое сообщение сериализуется в JSON и помещается в соответствующую очередь.
 */
@Service
@RequiredArgsConstructor
public class MessageSenderService {

    private static final Logger log = LoggerFactory.getLogger(MessageSenderService.class);

    public static final String PAYMENT_REQUEST_QUEUE = "enrollment.payment.request";
    public static final String ENROLLMENT_REQUEST_QUEUE = "enrollment.enrollment.request";

    private final JmsTemplate jmsTemplate;

    public void sendPaymentRequest(PaymentRequestMessage message) {
        log.info("Sending PaymentRequestMessage to queue '{}': applicationId={}",
                PAYMENT_REQUEST_QUEUE, message.applicationId());
        jmsTemplate.convertAndSend(PAYMENT_REQUEST_QUEUE, message);
    }

    public void sendEnrollmentRequest(EnrollmentRequestMessage message) {
        log.info("Sending EnrollmentRequestMessage to queue '{}': applicationId={}",
                ENROLLMENT_REQUEST_QUEUE, message.applicationId());
        jmsTemplate.convertAndSend(ENROLLMENT_REQUEST_QUEUE, message);
    }
}
