package com.r2s.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.auth.service.impl.AuthServiceImpl;
import com.r2s.core.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthServiceImpl authServiceImpl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. register_callsServiceWithExactPayload_whenBodyValid
    @Test
    void register_callsServiceWithExactPayload_whenBodyValid() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "123", "a@a.com", "Name", Role.ROLE_USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authServiceImpl).register(refEq(request));
    }

    // 2. register_returns200_whenBodyValid
    @Test
    void register_returns200_whenBodyValid() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "123", "a@a.com", "Name", null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    // 3. login_callsServiceWithExactPayload_whenCredentialsValid
    @Test
    void login_callsServiceWithExactPayload_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest("user", "123");
        when(authServiceImpl.login(any())).thenReturn(new AuthResponse("token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authServiceImpl).login(refEq(request));
    }

    // 4. login_returns200_andToken_whenCredentialsValid
    @Test
    void login_returns200_andToken_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest("user", "123");

        String mockJwt = "header.payload.signature";
        AuthResponse response = new AuthResponse(mockJwt);

        when(authServiceImpl.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(result -> {
                    String token = JsonPath.read(
                            result.getResponse().getContentAsString(), "$.token"
                    );
                    assertEquals(3, token.split("\\.").length, "Wrong JWT format");
                });
    }

    // 5. login_returns401_whenInvalidCredentials
    @Test
    void login_returns401_whenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("user", "wrong");
        when(authServiceImpl.login(any()))
                .thenThrow(new BadCredentialsException("Invalid password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    // 6. register_returns400_whenPayloadInvalid (Thêm mới)
    @Test
    void register_returns400_whenPayloadInvalid() throws Exception {

        RegisterRequest request = new RegisterRequest("", "", "invalid-email", "", Role.ROLE_USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value(notNullValue()))
                .andExpect(jsonPath("$.email").value(notNullValue()))
                .andExpect(jsonPath("$.password").value(notNullValue()));
    }

    // 7. login_returns400_whenPayloadInvalid
    @Test
    void login_returns400_whenPayloadInvalid() throws Exception {
        // send empty username and password
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value(notNullValue()))
                .andExpect(jsonPath("$.password").value(notNullValue()));
    }
}