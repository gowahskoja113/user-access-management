package com.r2s.user;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.UserService;
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

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // === TEST getAllUsers ===
    @Test
    void getAllUsers_shouldReturnListOfUserResponses() {
        // GIVEN
        User user1 = User.builder().username("son").email("son@gmail.com").role(Role.ROLE_USER).build();
        User user2 = User.builder().username("admin").email("admin@gmail.com").role(Role.ROLE_ADMIN).build();
        List<User> mockUsers = List.of(user1, user2);

        // Mock Repo
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Mock Mapper
        when(userMapper.toUserResponse(user1)).thenReturn(new UserResponse(Role.ROLE_USER, "son@gmail.com", null, "son"));
        when(userMapper.toUserResponse(user2)).thenReturn(new UserResponse(Role.ROLE_ADMIN, "admin@gmail.com", null, "admin"));

        // WHEN
        List<UserResponse> result = userService.getAllUsers();

        // THEN
        assertEquals(2, result.size());
        assertEquals("son", result.get(0).username());
        assertEquals("admin", result.get(1).username());

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).toUserResponse(any());
    }

    @Test
    void createUser_shouldSaveAndReturnUser_whenUsernameNotExists() {
        UserRequest request = new UserRequest("john", "1234", "John Doe", "john@example.com", Role.ROLE_USER);

        User savedUser = User.builder()
                .username("john")
                .password("encoded_1234")
                .name("John Doe")
                .email("john@example.com")
                .role(Role.ROLE_USER)
                .build();

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
        User mockUser = User.builder().username("son").email("son@gmail.com").role(Role.ROLE_USER).build();
        UserResponse mockResponse = new UserResponse(Role.ROLE_USER, "son@gmail.com", null, "son");

        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));

        when(userMapper.toUserResponse(mockUser)).thenReturn(mockResponse);

        UserResponse result = userService.getUserByUsername("son");

        assertEquals("son", result.username());

        verify(userRepository, times(1)).findByUsername("son");
        verify(userMapper, times(1)).toUserResponse(mockUser);
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
        User mockUser = User.builder().username("son").email("son@gmail.com").name("old name").role(Role.ROLE_USER).build();
        UpdateUserRequest update = UpdateUserRequest.builder().name("new son's name").email("newson@gmail.com").build();
        UserResponse expectedResponse = new UserResponse(Role.ROLE_USER, "newson@gmail.com", "new son's name", "son");

        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse result = userService.updateUser("son", update);

        assertEquals("new son's name", result.name());
        assertEquals("newson@gmail.com", result.email());

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).save(mockUser);
        verify(userMapper, times(1)).toUserResponse(any(User.class));
    }

    // === TEST deleteUser ===
    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        User mockUser = User.builder().username("son").build();
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
        User mockUser = User.builder().username("son").build();
        when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        Mockito.doThrow(new RuntimeException("DB error")).when(userRepository).delete(mockUser);

        assertThrows(RuntimeException.class, () -> userService.deleteUser("son"));

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).delete(mockUser);
    }
}