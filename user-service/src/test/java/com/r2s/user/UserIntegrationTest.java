package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 1. Chỉ cần SpringBootTest + MockMvc là đủ
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Tự động dọn dẹp DB H2 sau mỗi bài test
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldSaveToH2_andReturnSuccess() throws Exception {
        // GIVEN
        UserRequest request = new UserRequest("h2_user", "123456", "H2 User", "h2@gmail.com", Role.ROLE_USER);

        // WHEN
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // THEN (Check API trả về)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("h2_user"));

        // THEN (Check Database H2 - Logic Integration y hệt DB thật)
        User savedUser = userRepository.findByUsername("h2_user").orElseThrow();
        assert savedUser.getEmail().equals("h2@gmail.com");
    }

//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void createUser_shouldReturn400_whenUsernameExists() throws Exception {
//        // GIVEN: Lưu trước 1 thằng vào H2
//        User existingUser = User.builder()
//                .username("duplicate")
//                .password(passwordEncoder.encode("1234"))
//                .email("exist@gmail.com")
//                .name("Người Dùng Mẫu")
//                .role(Role.ROLE_USER)
//                .build();
//
//        userRepository.save(existingUser);
//
//        // WHEN: Tạo trùng tên
//        UserRequest request = new UserRequest("duplicate", "123456", "Any Name", "any@gmail.com", Role.ROLE_USER);
//
//        // THEN
//        mockMvc.perform(post("/api/users/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
}