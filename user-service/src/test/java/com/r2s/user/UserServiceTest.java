package com.r2s.user;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserMapper userMapper = new UserMapper();

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.setUserRepository(userRepository);
        userService.setUserMapper(userMapper);
        userService.setPasswordEncoder(passwordEncoder);
    }

    // === TEST getAllUsers ===
    @Test
    void getAllUsers_shouldReturnListOfUserResponses() {
        // GIVEN
        User user1 = new User();
        user1.setUsername("son");
        user1.setEmail("son@gmail.com");
        user1.setRole(Role.ROLE_USER);

        User user2 = new User();
        user2.setUsername("admin");
        user2.setEmail("admin@gmail.com");
        user2.setRole(Role.ROLE_ADMIN);
        List<User> mockUsers = List.of(user1, user2);

        // Mock Repo
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Expected results (UserMapper logic is simple, so we can create expected responses directly)
        List<UserResponse> expected = List.of(
            new UserResponse(Role.ROLE_USER, "son@gmail.com", null, "son"),
            new UserResponse(Role.ROLE_ADMIN, "admin@gmail.com", null, "admin")
        );

        // WHEN
        List<UserResponse> result = userService.getAllUsers();

        // THEN
        assertEquals(2, result.size());
        assertEquals("son", result.get(0).username());
        assertEquals("admin", result.get(1).username());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void createUser_shouldSaveAndReturnUser_whenUsernameNotExists() {
        UserRequest request = new UserRequest("john", "1234", "John Doe", "john@example.com", Role.ROLE_USER);

        User savedUser = new User();
        savedUser.setUsername("john");
        savedUser.setPassword("encoded_1234");
        savedUser.setName("John Doe");
        savedUser.setEmail("john@example.com");
        savedUser.setRole(Role.ROLE_USER);

        // Mock
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        when(passwordEncoder.encode("1234")).thenReturn("encoded_1234");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(request);

        assertEquals("john", result.getUsername());
        assertEquals("encoded_1234", result.getPassword());
        assertEquals("John Doe", result.getName());

        verify(userRepository, times(1)).findByUsername("john");
        verify(passwordEncoder, times(1)).encode("1234");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // === TEST getUserByUsername ===
    @Test
    void getUserByUsername_shouldReturnUserResponse_whenUserExists() {
        User mockUser = new User();
        mockUser.setUsername("son");
        mockUser.setEmail("son@gmail.com");
        mockUser.setRole(Role.ROLE_USER);
        UserResponse mockResponse = new UserResponse(Role.ROLE_USER, "son@gmail.com", null, "son");

        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));

        // userMapper is now a real instance, so toUserResponse will be called directly

        UserResponse result = userService.getUserByUsername("son");

        assertEquals("son", result.username());

        verify(userRepository, times(1)).findByUsername("son");
    }

    @Test
    void getUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.getUserByUsername("unknown");
        });

        verify(userRepository, times(1)).findByUsername("unknown");
    }

    // === TEST updateUser ===
    @Test
    void updateUser_shouldUpdateAndReturnUserResponse_whenUserExists() {
        User mockUser = new User();
        mockUser.setUsername("son");
        mockUser.setEmail("son@gmail.com");
        mockUser.setName("old name");
        mockUser.setRole(Role.ROLE_USER);
        UpdateUserRequest update = new UpdateUserRequest("newson@gmail.com", "new son's name");
        UserResponse expectedResponse = new UserResponse(Role.ROLE_USER, "newson@gmail.com", "new son's name", "son");

        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // The real UserMapper will be used, so the expected response should match what it produces

        UserResponse result = userService.updateUser("son", update);

        assertEquals("new son's name", result.name());
        assertEquals("newson@gmail.com", result.email());

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).save(mockUser);
    }

    // === TEST deleteUser ===
    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        User mockUser = new User();
        mockUser.setUsername("son");
        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));

        userService.deleteUser("son");

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).delete(mockUser);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.deleteUser("unknown"));
        verify(userRepository, times(1)).findByUsername("unknown");
        verify(userRepository, Mockito.never()).delete(any());
    }

    @Test
    void deleteUser_shouldThrowException_whenRepositoryDeleteFails() {
        User mockUser = new User();
        mockUser.setUsername("son");
        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        Mockito.doThrow(new RuntimeException("DB error")).when(userRepository).delete(mockUser);

        assertThrows(RuntimeException.class, () -> userService.deleteUser("son"));

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).delete(mockUser);
    }
}