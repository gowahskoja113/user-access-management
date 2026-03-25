package com.r2s.auth.repository;


import com.r2s.auth.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
    /**
     * Tìm danh sách các sự kiện theo trạng thái (PENDING/PROCESSED/FAILED)
     */
    List<Outbox> findByStatus(String status);
}
