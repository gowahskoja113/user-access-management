package com.r2s.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.core.entity.Role;
import com.r2s.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback DB sau mỗi test
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean DB trước mỗi test
    }

    // 1. register_returns400_whenDuplicateUsername
    @Test
    void register_returns400_whenDuplicateUsername() throws Exception {
        // Dùng Builder tạo data test
        RegisterRequest req = RegisterRequest.builder()
                .username("dupUser")
                .password("123")
                .email("dup@test.com")
                .name("Duplicate")
                .role(Role.ROLE_USER)
                .build();

        // Lần 1: Thành công
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Lần 2: Trùng Username -> Mong đợi 400 (Bad Request)
        // (Phải có GlobalExceptionHandler ở trên mới pass được đoạn này)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 2. login_returns401_whenWrongPassword
    @Test
    void login_returns401_whenWrongPassword() throws Exception {
        // Đăng ký trước
        RegisterRequest req = new RegisterRequest("user", "pass123", "a@a.com", "A", null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Login sai pass
        LoginRequest loginReq = new LoginRequest("user", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    // 3. me_returns200_andEmail_whenBearerTokenValid
    @Test
    void me_returns200_andEmail_whenBearerTokenValid() throws Exception {
        // Code của bạn CHƯA CÓ endpoint /me, nên tui giả định endpoint là /api/users/me
        // Flow: Register -> Login lấy Token -> Gọi /me

        // 1. Register
        RegisterRequest req = new RegisterRequest("meuser", "123", "me@test.com", "Me", null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // 2. Login
        LoginRequest loginReq = new LoginRequest("meuser", "123");
        String responseContent = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn().getResponse().getContentAsString();

        // Parse token từ response JSON
        String token = objectMapper.readTree(responseContent).get("token").asText();

        // 3. Call Protected Endpoint (cần thay đúng endpoint trong code thật của bạn)
        mockMvc.perform(get("/api/users/me") // Thay bằng endpoint thật
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@test.com"));
    }

    // 4. register_returns200_andPersistUser_whenValid
    @Test
    void register_returns200_andPersistUser_whenValid() throws Exception {
        RegisterRequest req = new RegisterRequest("newuser", "123", "new@test.com", "New", Role.ROLE_USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // Verify DB
        assertEquals(1, userRepository.count());
        assertEquals("new@test.com", userRepository.findByUsername("newuser").get().getEmail());
    }

    // 5. login_returns200_andToken_whenCredentialsValid
    @Test
    void login_returns200_andToken_whenCredentialsValid() throws Exception {
        // Register
        RegisterRequest req = new RegisterRequest("loginuser", "123", "l@test.com", "L", null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Login
        LoginRequest loginReq = new LoginRequest("loginuser", "123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}