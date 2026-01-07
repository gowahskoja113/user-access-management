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
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
class UserIntegrationTest extends AbstractIntegrationTest {

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
    void createUser_shouldSaveToDb_andReturnSuccess() throws Exception {
        // GIVEN
        UserRequest request = new UserRequest("new_user", "123456", "New User", "new@gmail.com", Role.ROLE_USER);

        // WHEN
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // THEN (Check API trả về)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("new_user"));

        // THEN (Check Database - Logic Integration y hệt DB thật)
        User savedUser = userRepository.findByUsername("new_user").orElseThrow();
        assert savedUser.getEmail().equals("new@gmail.com");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn400_whenUsernameExists() throws Exception {
        // GIVEN: Lưu trước 1 user vào DB
        User existingUser = new User();
        existingUser.setUsername("duplicate");
        existingUser.setPassword(passwordEncoder.encode("1234"));
        existingUser.setEmail("exist@gmail.com");
        existingUser.setName("Người Dùng Mẫu");
        existingUser.setRole(Role.ROLE_USER);

        userRepository.save(existingUser);

        // WHEN: Tạo trùng tên
        UserRequest request = new UserRequest("duplicate", "123456", "Any Name", "any@gmail.com", Role.ROLE_USER);

        // THEN
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnList() throws Exception {
        // dọn db trước khi test
        userRepository.deleteAll();

        // GIVEN: Seed 2 users vào DB
        User user1 = new User();
        user1.setUsername("u1");
        user1.setPassword("p");
        user1.setName("n1");
        user1.setEmail("e1");
        user1.setRole(Role.ROLE_USER);
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("u2");
        user2.setPassword("p");
        user2.setName("n2");
        user2.setEmail("e2");
        user2.setRole(Role.ROLE_USER);
        userRepository.save(user2);

        // WHEN & THEN
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    @WithMockUser(username = "myuser", roles = "USER")
    void getMyProfile_shouldReturnCorrectInfo() throws Exception {
        // GIVEN
        User me = new User();
        me.setUsername("myuser");
        me.setPassword("pass");
        me.setName("My Name");
        me.setEmail("my@gmail.com");
        me.setRole(Role.ROLE_USER);
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
        User original = new User();
        original.setUsername("update_user");
        original.setPassword("pass");
        original.setName("Old Name");
        original.setEmail("old@gmail.com");
        original.setRole(Role.ROLE_USER);
        userRepository.save(original);

        UpdateUserRequest updateRequest = new UpdateUserRequest("new@gmail.com", "New Name");

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
        User toDelete = new User();
        toDelete.setUsername("todelete");
        toDelete.setPassword("pass");
        toDelete.setName("Del");
        toDelete.setEmail("del@gmail.com");
        toDelete.setRole(Role.ROLE_USER);
        userRepository.save(toDelete);

        // WHEN
        mockMvc.perform(delete("/api/users/todelete"))
                .andExpect(status().isNoContent());

        // THEN: Tìm trong DB
        assertFalse(userRepository.findByUsername("todelete").isPresent());
    }
}