package com.r2s.auth.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "auth.user.queue";

    @Bean
    public Queue userAuthQueue() {
        return new Queue(QUEUE, true);
    }
}