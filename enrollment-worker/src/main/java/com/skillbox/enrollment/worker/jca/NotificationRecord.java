package com.skillbox.enrollment.worker.jca;

import jakarta.resource.cci.Record;

public class NotificationRecord implements Record {

    private final String userEmail;
    private final String courseId;
    private final String operation;

    public NotificationRecord(String userEmail, String courseId, String operation) {
        this.userEmail = userEmail;
        this.courseId  = courseId;
        this.operation = operation;
    }

    public String getUserEmail() { return userEmail; }
    public String getCourseId()  { return courseId; }
    public String getOperation() { return operation; }

    @Override public String getRecordName()                        { return "NotificationRecord"; }
    @Override public void   setRecordName(String name)            {}
    @Override public String getRecordShortDescription()           { return operation + ":" + userEmail; }
    @Override public void   setRecordShortDescription(String d)   {}
    @Override public Object clone() throws CloneNotSupportedException { return super.clone(); }
    @Override public boolean equals(Object o) {
        if (!(o instanceof NotificationRecord r)) return false;
        return userEmail.equals(r.userEmail) && courseId.equals(r.courseId);
    }
    @Override public int hashCode() { return 31 * userEmail.hashCode() + courseId.hashCode(); }
}
