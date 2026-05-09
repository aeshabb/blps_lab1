import re

for file in ["enrollment-api/src/main/java/com/skillbox/enrollment/config/JmsConfig.java", "enrollment-worker/src/main/java/com/skillbox/enrollment/worker/config/JmsConfig.java"]:
    with open(file, "r") as f:
        text = f.read()

    new_bean = """    @Bean
    public ConnectionFactory jmsConnectionFactory() throws Exception {
        java.net.URI uri = new java.net.URI(amqpUrl);
        String userInfo = uri.getUserInfo();
        String cleanUrl = new java.net.URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
        JmsConnectionFactory factory = new JmsConnectionFactory(cleanUrl);
        if (userInfo != null) {
            String[] parts = userInfo.split(":");
            if (parts.length > 0) factory.setUsername(parts[0]);
            if (parts.length > 1) factory.setPassword(parts[1]);
        }
        return factory;
    }"""
    
    text = re.sub(r'    @Bean\n    public ConnectionFactory jmsConnectionFactory\(\) \{\n        return new JmsConnectionFactory\(amqpUrl\);\n    \}', new_bean, text)
    
    with open(file, "w") as f:
        f.write(text)
