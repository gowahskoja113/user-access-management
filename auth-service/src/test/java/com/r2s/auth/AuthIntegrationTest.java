package com.r2s.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.auth.repository.RoleRepository;
import com.r2s.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "JWT_SECRET=my_super_secret_key_for_unit_testing_only_123456",
        "jwt.expiration=3600000"
})
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

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName(RoleName.ROLE_USER).isEmpty()) {
            Role role = new Role();
            role.setName(RoleName.ROLE_USER);
            roleRepository.save(role);
        }
    }

    @Test
    void register_returns400_whenDuplicateUsername() throws Exception {
        RegisterRequest req = new RegisterRequest("dupUser", "123", "dup@test.com", "Duplicate", RoleName.ROLE_USER);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns401_whenWrongPassword() throws Exception {
        RegisterRequest req = new RegisterRequest("user", "pass123", "a@a.com", "A", null);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        LoginRequest loginReq = new LoginRequest("user", "wrongpass");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_returnsJwtToken_andChecksThreeParts_whenCredentialsValid() throws Exception {
        RegisterRequest req = new RegisterRequest("meuser", "123", "me@test.com", "Me", null);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        LoginRequest loginReq = new LoginRequest("meuser", "123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        // KIỂM TRA 3 BƯỚC TOKEN
        assertNotNull(token);
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT must have 3 parts: Header, Payload, Signature");
        assertThat(parts[0]).isNotEmpty();
        assertThat(parts[1]).isNotEmpty();
        assertThat(parts[2]).isNotEmpty();
    }

    @Test
    void register_returns200_andPersistUser_whenValid() throws Exception {
        RegisterRequest req = new RegisterRequest("newuser", "123", "new@test.com", "New", RoleName.ROLE_USER);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        var savedUser = userRepository.findByUsername("newuser").orElseThrow();
        assertEquals("new@test.com", savedUser.getEmail());

        // Check role trong Set<Role>
        boolean hasRole = savedUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals(RoleName.ROLE_USER));
        assertTrue(hasRole);
    }

    @Test
    void register_returns400_whenFieldsAreBlank() throws Exception {
        RegisterRequest req = new RegisterRequest("", "", "invalid-email", "", RoleName.ROLE_USER);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns400_whenFieldsAreBlank() throws Exception {
        LoginRequest req = new LoginRequest("", "");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}