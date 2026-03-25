package com.r2s.auth.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.entity.User;
import com.r2s.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthEventListener {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "auth.sync.from.user.queue")
    @Transactional
    public void handleUserSyncFromProfile(String message) {
        log.info("📩 [Auth-Service] Nhận tín hiệu đồng bộ từ User-Service: {}", message);
        try {
            JsonNode node = objectMapper.readTree(message);
            UUID userId = UUID.fromString(node.get("id").asText());

            // Tự suy luận Event dựa trên nội dung (hoặc ge có thể gửi kèm eventType trong payload)
            if (node.has("email")) { // Giả sử đây là UPDATE
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    user.setEmail(node.get("email").asText());
                    user.setName(node.get("fullName").asText());
                    userRepository.save(user);
                    log.info("✅ [Auth-Service] Đã cập nhật Email/Name cho User ID: {}", userId);
                }
            } else { // Giả sử đây là DELETE (payload chỉ có ID)
                userRepository.deleteById(userId);
                log.info("🗑️ [Auth-Service] Đã xóa User ID: {}", userId);
            }

        } catch (Exception e) {
            log.error("❌ [Auth-Service] Lỗi xử lý đồng bộ ngược: {}", e.getMessage());
        }
    }
}