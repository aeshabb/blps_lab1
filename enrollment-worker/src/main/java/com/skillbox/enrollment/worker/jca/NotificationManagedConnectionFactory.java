package com.skillbox.enrollment.worker.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

public class NotificationManagedConnectionFactory implements ManagedConnectionFactory {

    private RestTemplate restTemplate;
    private String crmBaseUrl = "http://localhost:8080";

    public void setRestTemplate(RestTemplate rt)  { this.restTemplate = rt; }
    public void setCrmBaseUrl(String url)          { this.crmBaseUrl = url; }

    @Override public Object createConnectionFactory(ConnectionManager cm) throws ResourceException {
        return new NotificationConnectionFactory(this, cm);
    }
    @Override public Object createConnectionFactory() throws ResourceException {
        return new NotificationConnectionFactory(this, null);
    }
    @Override public ManagedConnection createManagedConnection(Subject s, ConnectionRequestInfo i) throws ResourceException {
        return new NotificationManagedConnection(restTemplate, crmBaseUrl);
    }
    @Override public ManagedConnection matchManagedConnections(Set set, Subject s, ConnectionRequestInfo i) throws ResourceException {
        Iterator it = set.iterator();
        return it.hasNext() ? (ManagedConnection) it.next() : null;
    }
    @Override public void        setLogWriter(PrintWriter out) throws ResourceException {}
    @Override public PrintWriter getLogWriter()                throws ResourceException { return null; }
}
