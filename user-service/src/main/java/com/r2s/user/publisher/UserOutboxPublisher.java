package com.r2s.user.publisher;

import com.r2s.user.entity.Outbox;
import com.r2s.user.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserOutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public static final String UPDATE_ROUTING_KEY = "user.updated.routing.key";
    public static final String DELETE_ROUTING_KEY = "user.deleted.routing.key";

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishUserEvents() {
        List<Outbox> events = outboxRepository.findByStatus("PENDING");
        if (events.isEmpty()) return;

        for (Outbox event : events) {
            try {
                // Sử dụng EXCHANGE chung "user.exchange"
                // Routing key: dựa vào event_type (USER_UPDATED hoặc USER_DELETED)
                String routingKey = event.getEventType().equals("USER_UPDATED") ? UPDATE_ROUTING_KEY : DELETE_ROUTING_KEY;

                rabbitTemplate.convertAndSend("user.exchange", routingKey, event.getPayload());

                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("[User-Outbox] Đã đẩy sự kiện {} sang Auth thành công!", event.getEventType());
            } catch (Exception e) {
                log.error("[User-Outbox] Lỗi đẩy tin nhắn: {}", e.getMessage());
            }
        }
    }
}