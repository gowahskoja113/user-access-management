package com.r2s.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.core.entity.Role;
import com.r2s.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. register_returns400_whenDuplicateUsername
    @Test
    void register_returns400_whenDuplicateUsername() throws Exception {

        RegisterRequest req = new RegisterRequest("dupUser", "123", "dup@test.com", "Duplicate", Role.ROLE_USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // register again with same username
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    // 2. login_returns401_whenWrongPassword
    @Test
    void login_returns401_whenWrongPassword() throws Exception {
        // Register user
        RegisterRequest req = new RegisterRequest("user", "pass123", "a@a.com", "A", null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Login with wrong password
        LoginRequest loginReq = new LoginRequest("user", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    // 3. me_returns200_andEmail_whenBearerTokenValid
    @Test
    void login_returnsJwtToken_whenCredentialsValid() throws Exception {
        //register
        RegisterRequest req = new RegisterRequest(
                "meuser", "123", "me@test.com", "Me", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        //login
        LoginRequest loginReq = new LoginRequest("meuser", "123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        //verify token format
        String token = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("token")
                .asText();

        assertThat(token.split("\\.").length).isEqualTo(3);
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
        var savedUser = userRepository.findByUsername("newuser").orElseThrow();

        // Assert important fields
        assertEquals("new@test.com", savedUser.getEmail());
        assertEquals("New", savedUser.getName());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
        assertNotNull(savedUser.getId());
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

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Verify token format
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3)
                .withFailMessage("Wrong JWT format");
    }

    // 6. register_returns400_whenFieldsAreBlank
    @Test
    void register_returns400_whenFieldsAreBlank() throws Exception {
        // Username và password để rỗng
        RegisterRequest req = new RegisterRequest("", "", "invalid-email", "", Role.ROLE_USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value(notNullValue()))
                .andExpect(jsonPath("$.username").value(notNullValue()));
    }

    // 7. login_returns400_whenFieldsAreBlank
    @Test
    void login_returns400_whenFieldsAreBlank() throws Exception {
        LoginRequest req = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value(notNullValue()))
                .andExpect(jsonPath("$.username").value(notNullValue()));
    }
}