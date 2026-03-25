package com.r2s.user.mapper;

import com.r2s.user.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxMapper {

    private final OutboxRepository outboxRepository;

    public void saveToOutbox(String aggregateType, String eventType, String payload) {
        com.r2s.user.entity.Outbox outbox = com.r2s.user.entity.Outbox.builder()
                .aggregateType(aggregateType)
                .eventType(eventType)
                .payload(payload)
                .status("PENDING")
                .createdAt(java.time.LocalDateTime.now())
                .build();
        outboxRepository.save(outbox);
    }
}
