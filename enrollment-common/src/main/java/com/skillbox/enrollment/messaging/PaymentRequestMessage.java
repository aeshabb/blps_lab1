package com.skillbox.enrollment.messaging;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Отправляется из enrollment-api в очередь enrollment.payment.request.
 * enrollment-worker получает и запрашивает платёжную ссылку у банка.
 */
public record PaymentRequestMessage(
        Long applicationId,
        BigDecimal amount,
        String userEmail,
        String userName
) implements Serializable {}
