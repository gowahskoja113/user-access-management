package com.r2s.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "user.exchange";
    public static final String AUTH_USER_QUEUE = "auth.user.queue";
    public static final String AUTH_SYNC_QUEUE = "auth.sync.from.user.queue";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue userAuthQueue() {
        return new Queue(AUTH_USER_QUEUE, true);
    }

    // Queue use for receiving sync signals from User-Service (email/name updates, deletions)
    @Bean
    public Queue authSyncQueue() {
        return new Queue(AUTH_SYNC_QUEUE, true);
    }

    // Binding for receiving new user creation events from User-Service
    @Bean
    public Binding bindingUpdateSync(Queue authSyncQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(authSyncQueue)
                .to(userExchange)
                .with("user.updated.routing.key");
    }

    // Nối Queue với Routing Key của sự kiện Xóa
    @Bean
    public Binding bindingDeleteSync(Queue authSyncQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(authSyncQueue)
                .to(userExchange)
                .with("user.deleted.routing.key");
    }
}