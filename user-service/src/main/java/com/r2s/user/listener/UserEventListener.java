package com.r2s.user.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.user.dto.request.UserRequest;

import com.r2s.user.entity.UserProfile;
import com.r2s.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

    private final UserProfileRepository userRepository;
    private final ObjectMapper objectMapper;

    // QUAN TRỌNG: Tên queue phải khớp với queue khai báo ở RabbitMQConfig
    @RabbitListener(queues = "auth.user.queue")
    @Transactional
    public void handleUserCreatedFromAuth(String message) {
        log.info("📩 [User-Service] Nhận tín hiệu tạo Profile từ Auth: {}", message);
        try {
            // 1. Giải mã JSON (Dùng chung DTO hoặc Map)
            UserRequest request = objectMapper.readValue(message, UserRequest.class);

            // 2. Kiểm tra Idempotency (Chống trùng dữ liệu)
            if (userRepository.existsById(request.id())) {
                log.warn("User ID {} đã tồn tại, bỏ qua", request.id());
                return;
            }

            // 3. Tạo Entity Profile (User bên User-Service)
            // Lưu ý: Không cần lưu Password ở bên này để bảo mật
            UserProfile userProfile = UserProfile.builder()
                    .id(request.id())
                    .username(request.username())
                    .email(request.email())
                    .fullName(request.fullName())
                    .build();

            userRepository.save(userProfile);
            log.info("✅ [User-Service] Đã tạo hồ sơ cho User: {} thành công!", userProfile.getUsername());

        } catch (Exception e) {
            log.error("❌ [User-Service] Lỗi xử lý tin nhắn: {}", e.getMessage());
        }
    }
}