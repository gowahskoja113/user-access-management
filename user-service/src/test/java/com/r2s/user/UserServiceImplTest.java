package com.r2s.user;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.response.UserResponse;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String TEST_USERNAME = "son";
    private static final String TEST_EMAIL = "son@gmail.com";
    private static final String NEW_EMAIL = "new@gmail.com";
    private static final String TEST_NAME = "Son";

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User createMockUser() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setEmail(TEST_EMAIL);
        user.setRole(Role.ROLE_USER);
        user.setPassword("rawPassword");
        return user;
    }

    private UserResponse createMockResponse() {
        return new UserResponse(Role.ROLE_USER, TEST_EMAIL, TEST_NAME, TEST_USERNAME);
    }

    @Nested
    @DisplayName("Tests for getAllUsers")
    class GetAllUsersTests {
        @Test
        @DisplayName("Should return list of UserResponse when users exist")
        void getAllUsers_shouldReturnListOfUserResponses() {
            // GIVEN
            User user1 = createMockUser();
            User user2 = new User();
            user2.setUsername("admin");

            when(userRepository.findAll()).thenReturn(List.of(user1, user2));
            when(userMapper.toUserResponse(user1)).thenReturn(createMockResponse());
            when(userMapper.toUserResponse(user2)).thenReturn(new UserResponse(Role.ROLE_ADMIN, "admin@gmail.com", "Admin", "admin"));

            // WHEN
            List<UserResponse> result = userService.getAllUsers();

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).username()).isEqualTo(TEST_USERNAME);
        }
    }

    @Nested
    @DisplayName("Tests for createUser")
    class CreateUserTests {
        @Test
        @DisplayName("Should encode password and save user when valid")
        void createUser_shouldSaveAndReturnUserResponse_whenValid() {
            // GIVEN
            UserRequest request = new UserRequest(TEST_USERNAME, "1234", TEST_NAME, TEST_EMAIL, Role.ROLE_USER);
            User entityToSave = createMockUser();
            User savedEntity = createMockUser();

            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(entityToSave);
            when(passwordEncoder.encode("1234")).thenReturn("encoded_1234");
            when(userRepository.save(any(User.class))).thenReturn(savedEntity);
            when(userMapper.toUserResponse(savedEntity)).thenReturn(createMockResponse());

            // WHEN
            UserResponse result = userService.createUser(request);

            // THEN
            assertThat(result.username()).isEqualTo(TEST_USERNAME);

            // Verify that password was encoded before saving
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getPassword()).isEqualTo("encoded_1234");
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void createUser_shouldThrowException_whenUsernameExists() {
            UserRequest request = new UserRequest(TEST_USERNAME, "1234", TEST_NAME, TEST_EMAIL, Role.ROLE_USER);
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

            assertThrows(CustomException.class, () -> userService.createUser(request));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for getUserByUsername")
    class GetUserTests {
        @Test
        void getUserByUsername_shouldReturnUserResponse_whenUserExists() {
            User mockUser = createMockUser();
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(userMapper.toUserResponse(mockUser)).thenReturn(createMockResponse());

            UserResponse result = userService.getUserByUsername(TEST_USERNAME);

            assertThat(result.username()).isEqualTo(TEST_USERNAME);
        }

        @Test
        void getUserByUsername_shouldThrowException_whenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
            assertThrows(CustomException.class, () -> userService.getUserByUsername("unknown"));
        }
    }

    @Nested
    @DisplayName("Tests for updateUser")
    class UpdateUserTests {
        @Test
        @DisplayName("Should update specific fields and save")
        void updateUser_shouldUpdateAndReturnResponse() {
            // GIVEN
            User existingUser = createMockUser();
            UpdateUserRequest updateRequest = new UpdateUserRequest(NEW_EMAIL, "New Name");
            UserResponse updatedResponse = new UserResponse(Role.ROLE_USER, NEW_EMAIL, "New Name", TEST_USERNAME);

            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            when(userMapper.toUserResponse(existingUser)).thenReturn(updatedResponse);

            // WHEN
            UserResponse result = userService.updateUser(TEST_USERNAME, updateRequest);

            // THEN
            assertThat(result.email()).isEqualTo(NEW_EMAIL);

            // Verify that only specified fields were updated
            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();

            assertThat(capturedUser.getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(capturedUser.getName()).isEqualTo("New Name");
        }
    }

    @Nested
    @DisplayName("Tests for deleteUser")
    class DeleteUserTests {
        @Test
        void deleteUser_shouldDeleteUser_whenUserExists() {
            User mockUser = createMockUser();
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));

            userService.deleteUser(TEST_USERNAME);

            verify(userRepository).delete(mockUser);
        }

        @Test
        void deleteUser_shouldThrowException_whenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThrows(CustomException.class, () -> userService.deleteUser("unknown"));
            verify(userRepository, never()).delete(any());
        }
    }
}