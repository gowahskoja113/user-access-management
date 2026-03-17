package com.r2s.auth;

import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.auth.service.RegistrationService;
import com.r2s.auth.service.impl.AuthServiceImpl;
import com.r2s.auth.strategy.AuthenticationStrategy;
import com.r2s.core.entity.RoleName;
import com.r2s.core.exception.CustomException;
import com.r2s.core.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationStrategy localStrategy;

    @Mock
    private RegistrationService registrationService;

    private AuthServiceImpl authServiceImpl;

    @BeforeEach
    void setUp() {
        // Khởi tạo Service với các dependency mới
        authServiceImpl = new AuthServiceImpl(List.of(localStrategy), registrationService);
    }

    // --- CÁC TEST CASE CHO REGISTER (Hồi sinh từ bản gốc) ---

    // 1. register_returnsResponse_whenUsernameAvailable_withGivenRoleAdmin
    @Test
    void register_returnsResponse_whenUsernameAvailable_withGivenRoleAdmin() {
        RegisterRequest request = new RegisterRequest("admin", "pass", "admin@test.com", "Admin", RoleName.ROLE_ADMIN);
        UserResponse mockResponse = new UserResponse(new HashSet<>(), "admin@test.com", "Admin", "admin");

        when(registrationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        UserResponse response = authServiceImpl.register(request);

        assertNotNull(response);
        verify(registrationService).register(request);
    }

    // 2. register_returnsResponse_whenUsernameAvailable_defaultsRoleUser
    @Test
    void register_returnsResponse_whenUsernameAvailable_defaultsRoleUser() {
        RegisterRequest request = new RegisterRequest("user", "pass", "user@test.com", "User", null);
        UserResponse mockResponse = new UserResponse(new HashSet<>(), "user@test.com", "User", "user");

        when(registrationService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        UserResponse response = authServiceImpl.register(request);

        assertNotNull(response);
        verify(registrationService).register(request);
    }

    // 5. register_throws_whenUsernameExists (Logic cũ ném CustomException)
    @Test
    void register_throws_whenUsernameExists() {
        RegisterRequest request = new RegisterRequest("exist", "pass", "mail", "name", null);

        // Khi RegistrationService báo lỗi, AuthServiceImpl phải ném lỗi đó ra
        when(registrationService.register(any())).thenThrow(new CustomException("Username already exists"));

        assertThrows(CustomException.class, () -> authServiceImpl.register(request));
    }


    // --- CÁC TEST CASE CHO LOGIN (Hồi sinh từ bản gốc và cập nhật Strategy) ---

    // 3. login_throws_whenUsernameNotFound
    @Test
    void login_throws_whenUsernameNotFound() {
        LoginRequest request = new LoginRequest("unknown", "pass");

        when(localStrategy.supports("LOCAL")).thenReturn(true);
        when(localStrategy.authenticate(request)).thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () -> authServiceImpl.login(request));
    }

    // 4. login_throws_whenPasswordWrong
    @Test
    void login_throws_whenPasswordWrong() {
        LoginRequest request = new LoginRequest("user", "wrongpass");

        when(localStrategy.supports("LOCAL")).thenReturn(true);
        when(localStrategy.authenticate(request)).thenThrow(new BadCredentialsException("Invalid password"));

        assertThrows(BadCredentialsException.class, () -> authServiceImpl.login(request));
    }

    // 6. login_returnToken_whenCredentialsValid
    @Test
    void login_returnToken_whenCredentialsValid() {
        LoginRequest request = new LoginRequest("user", "pass");
        AuthResponse expectedResponse = new AuthResponse("header.payload.signature");

        when(localStrategy.supports("LOCAL")).thenReturn(true);
        when(localStrategy.authenticate(request)).thenReturn(expectedResponse);

        // Gọi hàm login mặc định (1 tham số)
        AuthResponse response = authServiceImpl.login(request);

        assertNotNull(response);
        assertEquals("header.payload.signature", response.token());
        // Kiểm tra định dạng JWT 3 phần như logic mới yêu cầu
        assertEquals(3, response.token().split("\\.").length);
    }

    // Test case bổ sung cho logic Strategy
    @Test
    void login_shouldThrowException_whenAuthTypeNotSupported() {
        LoginRequest request = new LoginRequest("user", "pass");
        when(localStrategy.supports("OAUTH2")).thenReturn(false);

        assertThrows(CustomException.class, () -> authServiceImpl.login(request, "OAUTH2"));
    }
}