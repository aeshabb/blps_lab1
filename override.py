with open("enrollment-worker/src/main/java/com/skillbox/enrollment/worker/jca/NotificationConnectionFactory.java", "r") as f:
    text = f.read()

method = """
    private javax.naming.Reference reference;
    @Override
    public void setReference(javax.naming.Reference reference) {
        this.reference = reference;
    }
    @Override
    public javax.naming.Reference getReference() {
        return reference;
    }
"""

if "getReference" not in text:
    text = text.replace("}", method + "}")
    with open("enrollment-worker/src/main/java/com/skillbox/enrollment/worker/jca/NotificationConnectionFactory.java", "w") as f:
        f.write(text)
