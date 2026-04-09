package com.r2s.auth.publisher;

import com.r2s.auth.entity.Outbox;
import com.r2s.auth.repository.OutboxRepository;
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
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE = "user.exchange";
    public static final String ROUTING_KEY = "user.created.routing.key";

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishEvents() {
        List<Outbox> events = outboxRepository.findByStatus("PENDING");

        if (events.isEmpty()) return;

        for (Outbox event : events) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event.getPayload());

                // Cập nhật trạng thái thành công
                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("🚀 [Auth-Outbox] Đã đẩy User {} sang RabbitMQ thành công!", event.getId());
            } catch (Exception e) {
                log.error("❌ [Auth-Outbox] Lỗi khi đẩy tin nhắn ID {}: {}", event.getId(), e.getMessage());
            }
        }
    }
}