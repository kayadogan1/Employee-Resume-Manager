package com.employeeresumemaneger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@Slf4j
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "resume.exchange";
    public static final String QUEUE_NAME = "resume.process.queue";
    public static final String ROUTING_KEY = "resume.routing.key";

    @Bean
    public MessageConverter messageConverter() {
        log.info("[RABBITMQ-CONFIG] Jackson JSON MessageConverter oluşturuluyor...");
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue resumeQueue() {
        log.info("[RABBITMQ-CONFIG] Kuyruk oluşturuluyor: {}", QUEUE_NAME);
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange resumeExchange() {
        log.info("[RABBITMQ-CONFIG] Exchange oluşturuluyor: {}", EXCHANGE_NAME);
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding resumeBinding(Queue resumeQueue, DirectExchange resumeExchange) {
        log.info("[RABBITMQ-CONFIG] Binding oluşturuluyor: {} -> {} (key: {})",
                QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        return BindingBuilder.bind(resumeQueue).to(resumeExchange).with(ROUTING_KEY);
    }
}
