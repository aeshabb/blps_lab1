package com.skillbox.enrollment.worker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Session;

/**
 * JMS-конфигурация для enrollment-worker (Node 2).
 * Использует Apache Qpid JMS — реализацию JMS API поверх AMQP 1.0.
 *
 * Ключевые настройки:
 * - sessionAcknowledgeMode = SESSION_TRANSACTED: сообщение подтверждается (ack)
 *   только после успешного commit транзакции. Если обработка упадёт — RabbitMQ
 *   повторно доставит сообщение (гарантия at-least-once).
 * - transactionManager: Spring JpaTransactionManager координирует JMS + JDBC,
 *   обеспечивая согласованность между подтверждением сообщения и обновлением БД.
 */
@Configuration
@EnableJms
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

    /**
     * Фабрика для контейнеров JMS-слушателей с транзакционной сессией.
     * SESSION_TRANSACTED означает, что acknowledge сообщения происходит в рамках
     * той же транзакции, что и запись в БД — это обеспечивает согласованность.
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory jmsConnectionFactory,
            MessageConverter jacksonJmsMessageConverter,
            PlatformTransactionManager transactionManager) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(jmsConnectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter);
        factory.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        factory.setTransactionManager(transactionManager);
        factory.setConcurrency("1-3");
        return factory;
    }
}
