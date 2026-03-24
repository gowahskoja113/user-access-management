package com.r2s.auth.outbox;

import com.r2s.core.entity.Outbox;
import com.r2s.core.repository.OutboxRepository;
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

    // Quét mỗi giây 1 lần để đảm bảo tính thời gian thực cao
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishEvents() {
        List<Outbox> events = outboxRepository.findByStatus("PENDING");

        if (events.isEmpty()) return;

        for (Outbox event : events) {
            try {
                // Gửi sang Exchange của RabbitMQ
                // EXCHANGE và ROUTING_KEY nên khớp với cấu hình bên User-service
                rabbitTemplate.convertAndSend("user.exchange", "user.created.routing.key", event.getPayload());

                // Cập nhật trạng thái thành công
                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);

                log.info("🚀 [Auth-Outbox] Đã đẩy User {} sang RabbitMQ thành công!", event.getId());
            } catch (Exception e) {
                log.error("❌ [Auth-Outbox] Lỗi khi đẩy tin nhắn ID {}: {}", event.getId(), e.getMessage());
                // Không đổi trạng thái để lần quét sau (sau 1s) thử lại
            }
        }
    }
}