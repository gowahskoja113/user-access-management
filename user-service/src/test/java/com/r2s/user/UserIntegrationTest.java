package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnList() throws Exception {
        //don db truoc khi test
        userRepository.deleteAll();

        // GIVEN: Seed 2 users vào DB
        userRepository.save(User.builder().username("u1").password("p").name("n1").email("e1").role(Role.ROLE_USER).build());
        userRepository.save(User.builder().username("u2").password("p").name("n2").email("e2").role(Role.ROLE_USER).build());

        // WHEN & THEN
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    @WithMockUser(username = "myuser", roles = "USER")
    void getMyProfile_shouldReturnCorrectInfo() throws Exception {
        // GIVEN: Phải lưu thằng "myuser" vào DB thật trước thì mới tìm thấy
        User me = User.builder()
                .username("myuser")
                .password("pass")
                .name("My Name")
                .email("my@gmail.com")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(me);

        // WHEN & THEN
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("myuser"))
                .andExpect(jsonPath("$.name").value("My Name"));
    }

    @Test
    @WithMockUser(username = "update_user", roles = "USER")
    void updateMyProfile_shouldChangeDataInDB() throws Exception {
        // GIVEN
        User original = User.builder()
                .username("update_user")
                .password("pass")
                .name("Old N    ame")
                .email("old@gmail.com")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(original);

        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .name("New Name")
                .email("new@gmail.com")
                .build();

        // WHEN
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));

        // THEN: Check DB xem đã đổi thật chưa
        User updated = userRepository.findByUsername("update_user").orElseThrow();
        assertTrue(updated.getName().equals("New Name"));
        assertTrue(updated.getEmail().equals("new@gmail.com"));
    }

    // === 5. TEST XÓA (DELETE) ===
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldRemoveFromDB() throws Exception {
        // GIVEN
        User toDelete = User.builder()
                .username("todelete")
                .password("pass")
                .name("Del")
                .email("del@gmail.com")
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(toDelete);

        // WHEN
        mockMvc.perform(delete("/api/users/todelete"))
                .andExpect(status().isNoContent()); // Hoặc isOk() tuỳ Controller của bạn trả về 204 hay 200

        // THEN: Tìm trong DB phải không thấy nữa
        assertFalse(userRepository.findByUsername("todelete").isPresent());
    }
}