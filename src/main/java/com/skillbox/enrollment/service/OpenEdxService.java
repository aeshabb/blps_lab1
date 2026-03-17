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

    @Value("${services.openedx.url:http://localhost:${server.port:8080}/api/mock/openedx}")
    private String openEdxUrl;

    public OpenEdxService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean enrollUser(String email, String courseId) {
        log.info("Sending Open edX enrollment request: Enrolling user {} in course {}", email, courseId);
        
        try {
            Map<String, String> request = Map.of(
                "email", email,
                "courseId", courseId
            );
            
            ResponseEntity<Void> response = restTemplate.postForEntity(
                openEdxUrl + "/enroll", 
                request, 
                Void.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Failed to make request to Open edX API: {}", e.getMessage());
            return false;
        }
    }
}