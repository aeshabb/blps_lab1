import re

with open('/home/aeshabb/ITMO/blps/lab2/pom.xml', 'r') as f:
    text = f.read()

start = text.find('<dependencies>')
end = text.find('</dependencies>') + len('</dependencies>')

clean_deps = """<dependencies>
                <!-- Security -->
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-security</artifactId>
                </dependency>
                <!-- JTA -->
                <dependency>
                        <groupId>jakarta.transaction</groupId>
                        <artifactId>jakarta.transaction-api</artifactId>
                </dependency>
                <!-- Data / Web / Redis etc -->
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-data-jpa</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-validation</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-webmvc</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-data-redis</artifactId>
                </dependency>
                <dependency>
                        <groupId>com.fasterxml.jackson.core</groupId>
                        <artifactId>jackson-databind</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-cache</artifactId>
                </dependency>
                <dependency>
                        <groupId>org.postgresql</groupId>
                        <artifactId>postgresql</artifactId>
                        <scope>runtime</scope>
                </dependency>
                <dependency>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <optional>true</optional>
                </dependency>
                <!-- Test deps -->
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-data-jpa-test</artifactId>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-validation-test</artifactId>
                        <scope>test</scope>
                </dependency>
                <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-webmvc-test</artifactId>
                        <scope>test</scope>
                </dependency>
        </dependencies>"""

text = text[:start] + clean_deps + text[end:]

with open('/home/aeshabb/ITMO/blps/lab2/pom.xml', 'w') as f:
    f.write(text)
