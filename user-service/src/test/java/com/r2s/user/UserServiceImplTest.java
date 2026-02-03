package com.r2s.user;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // === TEST getAllUsers ===
    @Test
    void getAllUsers_shouldReturnListOfUserResponses() {
        // GIVEN
        User user1 = new User();
        user1.setUsername("son");
        User user2 = new User();
        user2.setUsername("admin");
        List<User> mockUsers = List.of(user1, user2);

        UserResponse res1 = new UserResponse(Role.ROLE_USER, "son@gmail.com", "Son", "son");
        UserResponse res2 = new UserResponse(Role.ROLE_ADMIN, "admin@gmail.com", "Admin", "admin");

        // Mock Behavior
        when(userRepository.findAll()).thenReturn(mockUsers);
        when(userMapper.toUserResponse(user1)).thenReturn(res1);
        when(userMapper.toUserResponse(user2)).thenReturn(res2);

        // WHEN
        List<UserResponse> result = userService.getAllUsers();

        // THEN
        assertEquals(2, result.size());
        assertEquals("son", result.get(0).username());
        assertEquals("admin", result.get(1).username());

        verify(userRepository, times(1)).findAll();
    }

    // === TEST createUser ===
    @Test
    void createUser_shouldSaveAndReturnUserResponse_whenValid() {
        // GIVEN
        UserRequest request = new UserRequest("john", "1234", "John Doe", "john@example.com", Role.ROLE_USER);

        User entityToSave = new User();
        User savedEntity = new User();
        savedEntity.setUsername("john");

        UserResponse expectedResponse = new UserResponse(Role.ROLE_USER, "john@example.com", "John Doe", "john");

        // Mock Behavior
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        // 2. Map request -> entity
        when(userMapper.toEntity(request)).thenReturn(entityToSave);

        // 3. Encode pass
        when(passwordEncoder.encode("1234")).thenReturn("encoded_1234");

        // 4. Save & Map ngược lại response
        when(userRepository.save(entityToSave)).thenReturn(savedEntity);
        when(userMapper.toUserResponse(savedEntity)).thenReturn(expectedResponse);

        // WHEN
        UserResponse result = userService.createUser(request);

        // THEN
        assertEquals("john", result.username());
        assertEquals("john@example.com", result.email());

        // Verify
        verify(userRepository).existsByUsername("john");
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(entityToSave);
    }

    @Test
    void createUser_shouldThrowException_whenUsernameExists() {
        UserRequest request = new UserRequest("john", "1234", "John Doe", "john@example.com", Role.ROLE_USER);

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(CustomException.class, () -> userService.createUser(request));

        verify(userRepository, never()).save(any());
    }

    // === TEST getUserByUsername ===
    @Test
    void getUserByUsername_shouldReturnUserResponse_whenUserExists() {
        User mockUser = new User();
        UserResponse mockResponse = new UserResponse(Role.ROLE_USER, "son@gmail.com", "Son", "son");

        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserResponse(mockUser)).thenReturn(mockResponse);

        UserResponse result = userService.getUserByUsername("son");

        assertEquals("son", result.username());
        verify(userRepository).findByUsername("son");
    }

    @Test
    void getUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> userService.getUserByUsername("unknown"));
    }

    // === TEST updateUser ===
    @Test
    void updateUser_shouldUpdateAndReturnResponse() {
        User mockUser = new User();
        mockUser.setUsername("son");

        UpdateUserRequest update = new UpdateUserRequest("new@gmail.com", "new name");
        UserResponse expectedResponse = new UserResponse(Role.ROLE_USER, "new@gmail.com", "new name", "son");

        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toUserResponse(mockUser)).thenReturn(expectedResponse);

        UserResponse result = userService.updateUser("son", update);

        assertEquals("new name", result.name());
        verify(userRepository).save(mockUser);
    }

    // === TEST deleteUser ===
    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        User mockUser = new User();
        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));

        userService.deleteUser("son");

        verify(userRepository).delete(mockUser);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> userService.deleteUser("unknown"));

        verify(userRepository, never()).delete(any());
    }
}