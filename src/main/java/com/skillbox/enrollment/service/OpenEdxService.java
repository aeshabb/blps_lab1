package com.skillbox.enrollment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OpenEdxService {
    private static final Logger log = LoggerFactory.getLogger(OpenEdxService.class);
    private final RestTemplate restTemplate;

    @Value("${services.openedx.url:https://jsonplaceholder.typicode.com/posts}")
    private String openEdxUrl;

    public OpenEdxService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean enrollUser(String email, String courseId) {
        log.info("Sending Open edX enrollment request to External API: {} / User {}, Course {}", openEdxUrl, email, courseId);
        
        try {
            Map<String, String> request = Map.of(
                "student_email", email,
                "course_details", courseId,
                "action", "enroll"
            );
            
            // Отправляем реальный HTTP POST запрос в интернет
            ResponseEntity<String> response = restTemplate.postForEntity(
                openEdxUrl, 
                request, 
                String.class
            );
            
            log.info("Received response from External API: Status {}, Body: {}", response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to make request to Open edX API: {}", e.getMessage());
            return false;
        }
    }
}