package com.skillbox.enrollment.worker.jca;

import jakarta.resource.cci.InteractionSpec;
import java.io.Serializable;

public class NotificationInteractionSpec implements InteractionSpec, Serializable {

    public static final String SEND_ENROLLMENT_NOTIFICATION = "SEND_ENROLLMENT_NOTIFICATION";
    public static final String SEND_FAILURE_NOTIFICATION    = "SEND_FAILURE_NOTIFICATION";

    private final String functionName;

    public NotificationInteractionSpec(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionName() { return functionName; }
}
