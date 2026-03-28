package com.skillbox.enrollment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            log.error("Open edX auth token is not configured. Set OPENEDX_AUTH_TOKEN.");
            return false;
        }

        String enrollUrl = openEdxBaseUrl + "/api/enrollment/v1/enrollment";
        log.info("Sending Open edX enrollment request: endpoint={}, user={}, course={}", enrollUrl, username, courseId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!openEdxHostHeader.isBlank()) {
                headers.set("Host", openEdxHostHeader);
            }
            headers.setBearerAuth(openEdxAuthToken);

            Map<String, Object> request = Map.of(
                    "user", username,
                    "course_details", Map.of("course_id", courseId),
                    "mode", openEdxEnrollmentMode,
                    "is_active", true
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    enrollUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Open edX response: status={}, body={}", response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 409 || containsIgnoreCase(body, "already enrolled")) {
                return true;
            }
            log.error("Open edX enrollment API returned {}: {}", e.getStatusCode().value(), body);
            return false;
        } catch (Exception e) {
            log.error("Failed to make request to Open edX API: {}", e.getMessage());
            return false;
        }
    }

    private boolean registerIfNeeded(String username, String email) {
        String registrationUrl = openEdxBaseUrl + "/api/user/v1/account/registration/";
        log.info("Sending Open edX registration request: endpoint={}, user={}", registrationUrl, username);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if (!openEdxHostHeader.isBlank()) {
                headers.set("Host", openEdxHostHeader);
            }

            MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
            request.add("username", username);
            request.add("email", email);
            request.add("password", openEdxDefaultPassword);
            request.add("name", username);
            request.add("honor_code", "true");
            request.add("terms_of_service", "true");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    registrationUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 400 && containsIgnoreCase(body, "already")) {
                return true;
            }
            log.error("Open edX registration API returned {}: {}", e.getStatusCode().value(), body);
            return false;
        } catch (Exception e) {
            log.error("Failed to make request to Open edX registration API: {}", e.getMessage());
            return false;
        }
    }

    private String buildUsername(String email) {
        String localPart = email.split("@")[0].toLowerCase();
        String normalized = localPart.replaceAll("[^a-z0-9._-]", "");
        if (normalized.isBlank()) {
            return "student";
        }
        return normalized;
    }

    private boolean containsIgnoreCase(String source, String token) {
        return source != null && source.toLowerCase().contains(token.toLowerCase());
    }
}