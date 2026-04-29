package com.skillbox.enrollment.worker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class BankService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    private final RestTemplate restTemplate;

    @Value("${services.bank.url:http://localhost:${api.port:8080}/api/mock/bank}")
    private String bankServiceUrl;

    public BankService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generatePaymentLink(Long applicationId, BigDecimal amount) {
        log.info("Requesting payment link from Bank API for application {}, amount {}", applicationId, amount);
        try {
            Map<String, Object> request = Map.of(
                    "applicationId", applicationId,
                    "amount", amount
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    bankServiceUrl + "/generate-link", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("paymentLink");
            }
        } catch (Exception e) {
            log.error("Failed to make request to Bank API: {}", e.getMessage());
        }
        throw new RuntimeException("Could not generate payment link from Bank API");
    }
}
