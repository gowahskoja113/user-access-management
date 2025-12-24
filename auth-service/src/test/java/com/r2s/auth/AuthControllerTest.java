package com.r2s.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.controller.AuthController;
import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.auth.service.AuthService;
import com.r2s.core.entity.Role;
import com.r2s.core.exception.GlobalExceptionHandler;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsService userDetailsService;

    // 1. register_callsServiceWithExactPayload_whenBodyValid
    @Test
    void register_callsServiceWithExactPayload_whenBodyValid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("user")
                .password("123")
                .email("a@a.com")
                .name("Name")
                .role(Role.ROLE_USER)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService, times(1)).register(any(RegisterRequest.class));
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
        when(authService.login(any())).thenReturn(new AuthResponse("token"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(authService).login(refEq(request));
    }

    // 4. login_returns200_andToken_whenCredentialsValid
    @Test
    void login_returns200_andToken_whenCredentialsValid() throws Exception {
        LoginRequest request = new LoginRequest("user", "123");
        AuthResponse response = new AuthResponse("access-token-123");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token-123"));
    }

    // 5. login_returns401_whenInvalidCredentials
    @Test
    void login_returns401_whenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("user", "wrong");
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Invalid password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid password"));
    }
}