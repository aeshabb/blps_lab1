package com.skillbox.enrollment.worker.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class NotificationConnection implements Connection {

    private static final Logger log = LoggerFactory.getLogger(NotificationConnection.class);

    private final RestTemplate restTemplate;
    private final String crmBaseUrl;
    private boolean closed = false;

    public NotificationConnection(RestTemplate restTemplate, String crmBaseUrl) {
        this.restTemplate = restTemplate;
        this.crmBaseUrl   = crmBaseUrl;
    }

    public void sendNotification(NotificationRecord record) {
        if (closed) throw new IllegalStateException("Connection is closed");
        try {
            Map<String, Object> body = Map.of(
                    "operation", record.getOperation(),
                    "userEmail", record.getUserEmail(),
                    "courseId",  record.getCourseId()
            );
            restTemplate.postForEntity(crmBaseUrl + "/api/mock/crm/notify", body, Map.class);
            log.info("CRM notification sent via JCA: {}", record.getOperation());
        } catch (Exception e) {
            log.error("CRM JCA call failed: {}", e.getMessage());
        }
    }

    @Override public Interaction createInteraction() throws ResourceException {
        return new NotificationInteraction(this);
    }
    @Override public LocalTransaction getLocalTransaction() throws ResourceException {
        throw new ResourceException("Not supported");
    }
    @Override public ConnectionMetaData getMetaData() throws ResourceException {
        return new ConnectionMetaData() {
            public String getEISProductName()    { return "SkillboxCRM"; }
            public String getEISProductVersion() { return "1.0"; }
            public String getUserName()          { return "system"; }
        };
    }
    @Override public ResultSetInfo getResultSetInfo() throws ResourceException {
        throw new ResourceException("Not supported");
    }
    @Override public void close() { closed = true; }
}
