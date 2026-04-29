package com.skillbox.enrollment.worker.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class NotificationManagedConnection implements ManagedConnection {

    private final RestTemplate restTemplate;
    private final String crmBaseUrl;
    private final List<ConnectionEventListener> listeners = new ArrayList<>();

    public NotificationManagedConnection(RestTemplate restTemplate, String crmBaseUrl) {
        this.restTemplate = restTemplate;
        this.crmBaseUrl   = crmBaseUrl;
    }

    @Override public Object getConnection(Subject s, ConnectionRequestInfo i) throws ResourceException {
        return new NotificationConnection(restTemplate, crmBaseUrl);
    }
    @Override public void destroy()                                  throws ResourceException {}
    @Override public void cleanup()                                  throws ResourceException {}
    @Override public void associateConnection(Object c)              throws ResourceException {}
    @Override public void addConnectionEventListener(ConnectionEventListener l)    { listeners.add(l); }
    @Override public void removeConnectionEventListener(ConnectionEventListener l) { listeners.remove(l); }
    @Override public XAResource getXAResource()                      throws ResourceException {
        throw new ResourceException("XA not supported");
    }
    @Override public LocalTransaction getLocalTransaction()          throws ResourceException {
        throw new ResourceException("Local transactions not supported");
    }
    @Override public ManagedConnectionMetaData getMetaData()         throws ResourceException {
        return new ManagedConnectionMetaData() {
            public String getEISProductName()    { return "SkillboxCRM"; }
            public String getEISProductVersion() { return "1.0"; }
            public String getUserName()          { return "system"; }
            public int    getMaxConnections()    { return 10; }
        };
    }
    @Override public void       setLogWriter(PrintWriter out) {}
    @Override public PrintWriter getLogWriter()               { return null; }
}
