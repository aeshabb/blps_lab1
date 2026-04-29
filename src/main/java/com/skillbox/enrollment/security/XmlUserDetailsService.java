package com.skillbox.enrollment.security;

import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class XmlUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try (InputStream is = new ClassPathResource("users.xml").getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            NodeList users = doc.getElementsByTagName("user");

            for (int i = 0; i < users.getLength(); i++) {
                Element userElement = (Element) users.item(i);
                String uName = userElement.getAttribute("username");
                if (username.equals(uName)) {
                    String password = userElement.getAttribute("password");
                    String rolesStr = userElement.getAttribute("roles");
                    String privsStr = userElement.getAttribute("privileges");

                    List<String> authorities = new ArrayList<>();
                    if (!rolesStr.isEmpty()) {
                        for (String r : rolesStr.split(",")) authorities.add(r.trim());
                    }
                    if (!privsStr.isEmpty()) {
                        for (String p : privsStr.split(",")) authorities.add(p.trim());
                    }

                    return User.withUsername(username)
                            .password(password)
                            .authorities(authorities.toArray(new String[0]))
                            .build();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading users from XML", e);
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
