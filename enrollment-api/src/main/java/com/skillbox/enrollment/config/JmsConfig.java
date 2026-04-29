package com.skillbox.enrollment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import jakarta.jms.ConnectionFactory;

/**
 * Конфигурация JMS-клиента для отправки сообщений по протоколу AMQP 1.0.
 * Использует Apache Qpid JMS как реализацию JMS API поверх AMQP 1.0.
 * Брокер — RabbitMQ с включённым плагином rabbitmq_amqp1_0.
 */
@Configuration
public class JmsConfig {

    @Value("${rabbitmq.amqp.url:amqp://guest:guest@localhost:5672}")
    private String amqpUrl;

    @Bean
    public ConnectionFactory jmsConnectionFactory() {
        return new JmsConnectionFactory(amqpUrl);
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory jmsConnectionFactory,
                                   MessageConverter jacksonJmsMessageConverter) {
        JmsTemplate template = new JmsTemplate(jmsConnectionFactory);
        template.setMessageConverter(jacksonJmsMessageConverter);
        return template;
    }
}
