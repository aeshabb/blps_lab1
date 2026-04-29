package com.skillbox.enrollment.worker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OpenEdxService {

    private static final Logger log = LoggerFactory.getLogger(OpenEdxService.class);

    private final RestTemplate restTemplate;

    @Value("${services.openedx.base-url:http://localhost:18000}")
    private String openEdxBaseUrl;

    @Value("${services.openedx.host-header:mamochki}")
    private String openEdxHostHeader;

    @Value("${services.openedx.auth-token:}")
    private String openEdxAuthToken;

    @Value("${services.openedx.default-password:Lab1Pass_2026}")
    private String openEdxDefaultPassword;

    @Value("${services.openedx.enrollment-mode:audit}")
    private String openEdxEnrollmentMode;

    public OpenEdxService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean enrollUser(String email, String courseId) {
        String username = buildUsername(email);
        if (!registerIfNeeded(username, email)) {
            return false;
        }
        if (openEdxAuthToken.isBlank()) {
            log.error("Open edX auth token is not configured");
            return false;
        }

        String enrollUrl = openEdxBaseUrl + "/api/enrollment/v1/enrollment";
        log.info("Enrolling user={} in course={}", username, courseId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!openEdxHostHeader.isBlank()) headers.set("Host", openEdxHostHeader);
            headers.setBearerAuth(openEdxAuthToken);

            Map<String, Object> request = Map.of(
                    "user", username,
                    "course_details", Map.of("course_id", courseId),
                    "mode", openEdxEnrollmentMode,
                    "is_active", true
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    enrollUrl, HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 409 || containsIgnoreCase(body, "already enrolled")) {
                return true;
            }
            log.error("Open edX enrollment returned {}: {}", e.getStatusCode().value(), body);
            return false;
        } catch (Exception e) {
            log.error("Failed to call Open edX API: {}", e.getMessage());
            return false;
        }
    }

    private boolean registerIfNeeded(String username, String email) {
        String registrationUrl = openEdxBaseUrl + "/api/user/v1/account/registration/";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if (!openEdxHostHeader.isBlank()) headers.set("Host", openEdxHostHeader);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("username", username);
            body.add("email", email);
            body.add("password", openEdxDefaultPassword);
            body.add("name", username);
            body.add("honor_code", "true");
            body.add("terms_of_service", "true");

            ResponseEntity<String> response = restTemplate.exchange(
                    registrationUrl, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 400 && containsIgnoreCase(e.getResponseBodyAsString(), "already")) {
                return true;
            }
            log.error("Open edX registration returned {}: {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Failed to call Open edX registration API: {}", e.getMessage());
            return false;
        }
    }

    private String buildUsername(String email) {
        String local = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9._-]", "");
        return local.isBlank() ? "student" : local;
    }

    private boolean containsIgnoreCase(String src, String token) {
        return src != null && src.toLowerCase().contains(token.toLowerCase());
    }
}
