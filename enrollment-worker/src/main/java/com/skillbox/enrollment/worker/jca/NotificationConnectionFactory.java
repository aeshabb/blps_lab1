package com.skillbox.enrollment.worker.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import jakarta.resource.spi.ConnectionManager;

import java.io.Serializable;

public class NotificationConnectionFactory implements ConnectionFactory, Serializable {

    private final NotificationManagedConnectionFactory mcf;
    private final ConnectionManager connectionManager;

    public NotificationConnectionFactory(NotificationManagedConnectionFactory mcf, ConnectionManager cm) {
        this.mcf = mcf;
        this.connectionManager = cm;
    }

    @Override
    public Connection getConnection() throws ResourceException {
        if (connectionManager != null) {
            return (Connection) connectionManager.allocateConnection(mcf, null);
        }
        return (Connection) mcf.createManagedConnection(null, null).getConnection(null, null);
    }

    @Override public Connection getConnection(ConnectionSpec spec) throws ResourceException { return getConnection(); }

    @Override public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return new ResourceAdapterMetaData() {
            public String   getAdapterVersion()                 { return "1.0"; }
            public String   getAdapterVendorName()              { return "Skillbox"; }
            public String   getAdapterName()                    { return "CRM Notification Adapter"; }
            public String   getAdapterShortDescription()        { return "JCA adapter for CRM"; }
            public String   getSpecVersion()                    { return "2.1"; }
            public String[] getInteractionSpecsSupported()      { return new String[]{NotificationInteractionSpec.class.getName()}; }
            public boolean  supportsExecuteWithInputAndOutputRecord() { return true; }
            public boolean  supportsExecuteWithInputRecordOnly()      { return true; }
            public boolean  supportsLocalTransactionDemarcation()     { return false; }
        };
    }

    @Override public RecordFactory getRecordFactory() throws ResourceException {
        throw new ResourceException("Not supported");
    }
}
