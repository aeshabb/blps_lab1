package com.skillbox.enrollment.worker.jca;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jca.support.LocalConnectionFactoryBean;
import org.springframework.web.client.RestTemplate;

/**
 * Spring-конфигурация JCA-адаптера для enrollment-worker.
 * LocalConnectionFactoryBean позволяет использовать JCA CCI без Jakarta EE-контейнера.
 */
@Configuration
public class NotificationJcaConfig {

    @Value("${services.crm.url:http://localhost:${api.port:8080}}")
    private String crmBaseUrl;

    @Bean
    public NotificationManagedConnectionFactory notificationManagedConnectionFactory(RestTemplate restTemplate) {
        NotificationManagedConnectionFactory mcf = new NotificationManagedConnectionFactory();
        mcf.setRestTemplate(restTemplate);
        mcf.setCrmBaseUrl(crmBaseUrl);
        return mcf;
    }

    @Bean
    public ConnectionFactory crmConnectionFactory(
            NotificationManagedConnectionFactory notificationManagedConnectionFactory) throws ResourceException {
        LocalConnectionFactoryBean fb = new LocalConnectionFactoryBean();
        fb.setManagedConnectionFactory(notificationManagedConnectionFactory);
        try {
            fb.afterPropertiesSet();
        } catch (Exception e) {
            throw new ResourceException("Failed to create CRM ConnectionFactory", e);
        }
        return (ConnectionFactory) fb.getObject();
    }
}
