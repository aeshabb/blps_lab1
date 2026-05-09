import re

for file in ["enrollment-api/src/main/java/com/skillbox/enrollment/config/JmsConfig.java", "enrollment-worker/src/main/java/com/skillbox/enrollment/worker/config/JmsConfig.java"]:
    with open(file, "r") as f:
        text = f.read()

    text = re.sub(r'public MessageConverter jacksonJmsMessageConverter\(ObjectMapper objectMapper\)', 'public MessageConverter jacksonJmsMessageConverter()', text)
    text = re.sub(r'converter.setObjectMapper\(objectMapper\);', 'converter.setObjectMapper(new ObjectMapper());', text)
    
    with open(file, "w") as f:
        f.write(text)
