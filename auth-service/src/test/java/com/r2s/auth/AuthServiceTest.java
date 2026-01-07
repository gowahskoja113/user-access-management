package com.r2s.auth;

import com.r2s.auth.dto.request.LoginRequest;
import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.auth.dto.response.AuthResponse;
import com.r2s.auth.service.AuthService;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // 1. register_returnsResponse_whenUsernameAvailable_withGivenRoleAdmin
    @Test
    void register_returnsResponse_whenUsernameAvailable_withGivenRoleAdmin() {
        // Arrange
        RegisterRequest request = new RegisterRequest("admin", "pass", "admin@test.com", "Admin", Role.ROLE_ADMIN);

        when(userRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        // Act
        authService.register(request);

        // Assert
        verify(userRepo).save(argThat(user ->
                user.getUsername().equals("admin") &&
                        user.getRole().equals(Role.ROLE_ADMIN)
        ));
    }

    // 2. register_returnsResponse_whenUsernameAvailable_defaultsRoleUser
    @Test
    void register_returnsResponse_whenUsernameAvailable_defaultsRoleUser() {
        // Arrange (Role null -> Default ROLE_USER)
        RegisterRequest request = new RegisterRequest("user", "pass", "user@test.com", "User", null);
        when(userRepo.findByUsername("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        // Act
        authService.register(request);

        // Assert
        verify(userRepo).save(argThat(user ->
                user.getRole().equals(Role.ROLE_USER)
        ));
    }

    // 3. login_throws_whenUsernameNotFound
    @Test
    void login_throws_whenUsernameNotFound() {
        LoginRequest request = new LoginRequest("unknown", "pass");
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> authService.login(request));
    }

    // 4. login_throws_whenPasswordWrong
    @Test
    void login_throws_whenPasswordWrong() {
        LoginRequest request = new LoginRequest("user", "wrongpass");
        User user = new User();
        user.setUsername("user");
        user.setPassword("encodedRealPass");

        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encodedRealPass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    // 5. register_throws_whenUsernameExists
    @Test
    void register_throws_whenUsernameExists() {
        RegisterRequest request = new RegisterRequest("exist", "pass", "mail", "name", null);

        when(userRepo.findByUsername("exist")).thenReturn(Optional.of(new User()));

        assertThrows(CustomException.class, () -> authService.register(request));
    }

    // 6. login_returnToken_whenCredentialsValid
    @Test
    void login_returnToken_whenCredentialsValid() {
        LoginRequest request = new LoginRequest("user", "pass");
        User user = new User();
        user.setUsername("user");
        user.setPassword("encodedPass");

        when(userRepo.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("user")).thenReturn("dummy-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("dummy-token", response.token()); // Giả sử record AuthResponse có field token
    }
}