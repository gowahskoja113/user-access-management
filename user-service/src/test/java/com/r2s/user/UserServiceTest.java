package com.r2s.user;

import com.r2s.auth.dto.request.RegisterRequest;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

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
        Mockito.when(userRepository.findAll()).thenReturn(mockUsers);

        // Mock Mapper
        Mockito.when(userMapper.toUserResponse(user1)).thenReturn(new UserResponse(Role.ROLE_USER, "son@gmail.com", null, "son"));
        Mockito.when(userMapper.toUserResponse(user2)).thenReturn(new UserResponse(Role.ROLE_ADMIN, "admin@gmail.com", null, "admin"));

        // WHEN
        List<UserResponse> result = userService.getAllUsers();

        // THEN
        assertEquals(2, result.size());
        assertEquals("son", result.get(0).username());
        assertEquals("admin", result.get(1).username());

        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).toUserResponse(any()); // Verify mapper được gọi 2 lần
    }

//    @Test
//    void createUser_shouldSaveAndReturnUser() {
//        // 1. GIVEN (Chuẩn bị dữ liệu)
//        RegisterRequest request = RegisterRequest.builder()
//                .username("john")
//                .password("1234")
//                .name("John Doe")
//                .email("john@example.com")
//                .role(Role.ROLE_USER)
//                .build();
//
//        // Giả lập entity trả về từ DB
//        User savedUser = User.builder()
//                .username(request.username())
//                .password(request.password())
//                .name(request.name())
//                .email(request.email())
//                .role(request.role())
//                .build();
//
//        // Mock: Khi gọi save thì trả về savedUser
//        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);
//
//        // 2. WHEN (Gọi hàm createUser của UserService)
//        User result = userService.createUser(request);
//
//        // 3. THEN (Kiểm tra kết quả)
//        Assertions.assertEquals(savedUser.getUsername(), result.getUsername());
//        Assertions.assertEquals(savedUser.getEmail(), result.getEmail());
//        Assertions.assertEquals(savedUser.getRole(), result.getRole());
//
//        // Verify repo được gọi 1 lần
//        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
//    }

    // === TEST getUserByUsername ===
    @Test
    void getUserByUsername_shouldReturnUserResponse() {
        User mockUser = User.builder().username("son").email("son@gmail.com").role(Role.ROLE_USER).build();
        UserResponse mockResponse = new UserResponse(Role.ROLE_USER, "son@gmail.com", null, "son");

        Mockito.when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));

        // FIX QUAN TRỌNG: Mock mapper
        Mockito.when(userMapper.toUserResponse(mockUser)).thenReturn(mockResponse);

        UserResponse result = userService.getUserByUsername("son");

        assertEquals("son", result.username());

        verify(userRepository, times(1)).findByUsername("son");
        verify(userMapper, times(1)).toUserResponse(mockUser);
    }

    @Test
    void getUserByUsername_shouldThrowExceptionIfNotFound() {
        Mockito.when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.getUserByUsername("unknown");
        });

        verify(userRepository, times(1)).findByUsername("unknown");
    }

    // === TEST updateUser ===
    @Test
    void updateUser_shouldUpdateAndReturnUserResponse() {
        User mockUser = User.builder().username("son").email("son@gmail.com").name("old name").role(Role.ROLE_USER).build();
        UpdateUserRequest update = UpdateUserRequest.builder().name("new son's name").email("newson@gmail.com").build();
        UserResponse expectedResponse = new UserResponse(Role.ROLE_USER, "newson@gmail.com", "new son's name", "son");

        Mockito.when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(userMapper.toUserResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse result = userService.updateUser("son", update);

        assertEquals("new son's name", result.name());
        assertEquals("newson@gmail.com", result.email());

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).save(mockUser);
        verify(userMapper, times(1)).toUserResponse(any(User.class));
    }

    // === TEST deleteUser ===
    @Test
    void deleteUser_shouldDeleteIfExists() {
        User mockUser = User.builder().username("son").build();
        Mockito.when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));

        userService.deleteUser("son");

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).delete(mockUser);
    }

    @Test
    void deleteUser_shouldThrowExceptionIfNotFound() {
        Mockito.when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.deleteUser("unknown"));
        verify(userRepository, times(1)).findByUsername("unknown");
        verify(userRepository, Mockito.never()).delete(any());
    }

    @Test
    void deleteUser_shouldThrowIfDeleteFails() {
        User mockUser = User.builder().username("son").build();
        Mockito.when(userRepository.findByUsername("son")).thenReturn(Optional.of(mockUser));
        Mockito.doThrow(new RuntimeException("DB error")).when(userRepository).delete(mockUser);

        assertThrows(RuntimeException.class, () -> userService.deleteUser("son"));

        verify(userRepository, times(1)).findByUsername("son");
        verify(userRepository, times(1)).delete(mockUser);
    }
}