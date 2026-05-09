import re

with open("enrollment-worker/src/main/java/com/skillbox/enrollment/worker/jca/CrmNotificationService.java", "r") as f:
    text = f.read()

new_send = """    private void send(String userEmail, String courseId, String operation) {
        Connection conn = null;
        jakarta.resource.cci.Interaction interaction = null;
        try {
            conn = crmConnectionFactory.getConnection();
            var spec = new NotificationInteractionSpec(operation);
            var record = new NotificationRecord(userEmail, courseId, operation);
            interaction = conn.createInteraction();
            interaction.execute(spec, record);
            log.info("CRM notified via JCA: operation={}, email={}", operation, userEmail);
        } catch (ResourceException e) {
            log.error("JCA CRM notification failed: {}", e.getMessage());
        } finally {
            if (interaction != null) {
                try {
                    interaction.close();
                } catch (Exception e) {
                    log.error("Failed to close interaction: {}", e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.error("Failed to close connection: {}", e.getMessage());
                }
            }
        }
    }
"""

text = re.sub(r'    private void send\(String userEmail, String courseId, String operation\) \{.*?\n    \}', new_send, text, flags=re.DOTALL)
with open("enrollment-worker/src/main/java/com/skillbox/enrollment/worker/jca/CrmNotificationService.java", "w") as f:
    f.write(text)
