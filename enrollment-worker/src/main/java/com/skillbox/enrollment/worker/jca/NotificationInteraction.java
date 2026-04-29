package com.skillbox.enrollment.worker.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;

public class NotificationInteraction implements Interaction {

    private final NotificationConnection connection;

    public NotificationInteraction(NotificationConnection connection) {
        this.connection = connection;
    }

    @Override
    public boolean execute(InteractionSpec ispec, Record input, Record output) throws ResourceException {
        if (!(input instanceof NotificationRecord rec)) {
            throw new ResourceException("Expected NotificationRecord");
        }
        connection.sendNotification(rec);
        return true;
    }

    @Override
    public Record execute(InteractionSpec ispec, Record input) throws ResourceException {
        execute(ispec, input, null);
        return null;
    }

    @Override public void            close()        throws ResourceException {}
    @Override public Connection      getConnection()                        { return connection; }
    @Override public ResourceWarning getWarnings()  throws ResourceException { return null; }
    @Override public void            clearWarnings() throws ResourceException {}
}
