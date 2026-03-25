package com.r2s.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 1. Khai báo các hằng số (Tên phải khớp với bên User-Service)
    public static final String EXCHANGE = "user.exchange";
    public static final String AUTH_USER_QUEUE = "auth.user.queue";
    public static final String AUTH_SYNC_QUEUE = "auth.sync.from.user.queue";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // 3. Queue dùng để nhận tin nhắn tạo User (từ Auth sang User - nếu cần nghe ngược lại hoặc dùng cho mục đích khác)
    @Bean
    public Queue userAuthQueue() {
        return new Queue(AUTH_USER_QUEUE, true);
    }

    // 4. Queue dùng để nhận tin nhắn đồng bộ Update/Delete (từ User sang Auth)
    @Bean
    public Queue authSyncQueue() {
        return new Queue(AUTH_SYNC_QUEUE, true);
    }

    // 5. Binding: Nối Queue đồng bộ với Exchange thông qua Routing Key
    @Bean
    public Binding bindingSync(Queue authSyncQueue, TopicExchange userExchange) {
        // userExchange ở đây chính là cái Bean ge khai báo ở mục 2
        return BindingBuilder.bind(authSyncQueue)
                .to(userExchange)
                .with("user.#.routing.key");
    }
}