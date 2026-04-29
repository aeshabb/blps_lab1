package com.skillbox.enrollment.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Node 2 — enrollment-worker.
 * Получает JMS-сообщения из RabbitMQ (AMQP 1.0, Apache Qpid JMS),
 * обрабатывает их в распределённой транзакции (JMS + JDBC),
 * и уведомляет CRM-систему через JCA.
 *
 * Запуск: java -jar enrollment-worker.jar --DB_URL=... --RABBITMQ_AMQP_URL=... --API_BASE_URL=...
 */
@SpringBootApplication
@EntityScan("com.skillbox.enrollment.model")
@EnableJpaRepositories("com.skillbox.enrollment.repository")
public class WorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
