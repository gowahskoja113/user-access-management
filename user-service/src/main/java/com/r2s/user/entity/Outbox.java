package com.r2s.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "outbox")
public class Outbox {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String aggregateType;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}