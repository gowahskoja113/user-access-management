package com.r2s.user;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.core.entity.User;
import com.r2s.core.exception.CustomException;
import com.r2s.core.repository.UserRepository;
import com.r2s.core.response.UserResponse;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.mapper.UserMapper;
import com.r2s.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private User createMockUser() {
        Role r = new Role(); r.setName(RoleName.ROLE_USER);
        return User.builder().username("son").email("son@g.com").roles(Set.of(r)).password("pass").build();
    }

    private UserResponse createMockResponse() {
        Role r = new Role(); r.setName(RoleName.ROLE_USER);
        return new UserResponse(Set.of(r), "son@g.com", "Son", "son");
    }

    @Nested
    class GetAllUsersTests {
        @Test
        void getAllUsers_shouldReturnListOfUserResponses() {
            User user1 = createMockUser();
            when(userRepository.findAll()).thenReturn(List.of(user1));
            when(userMapper.toUserResponse(user1)).thenReturn(createMockResponse());
            assertThat(userService.getAllUsers()).hasSize(1);
        }
    }

    @Nested
    class CreateUserTests {
        @Test
        void createUser_shouldSaveAndReturnUserResponse_whenValid() {
            UserRequest request = new UserRequest("son", "123", "Son", "son@g.com", RoleName.ROLE_USER);
            User user = createMockUser();
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userMapper.toEntity(any())).thenReturn(user);
            when(userRepository.save(any())).thenReturn(user);
            when(userMapper.toUserResponse(any())).thenReturn(createMockResponse());
            assertThat(userService.createUser(request).username()).isEqualTo("son");
        }

        @Test
        void createUser_shouldThrowException_whenUsernameExists() {
            when(userRepository.existsByUsername(anyString())).thenReturn(true);
            assertThrows(CustomException.class, () -> userService.createUser(new UserRequest("son", "1", "S", "e", RoleName.ROLE_USER)));
        }
    }

    @Nested
    class GetUserTests {
        @Test
        void getUserByUsername_shouldReturnUserResponse_whenUserExists() {
            User user = createMockUser();
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
            when(userMapper.toUserResponse(any())).thenReturn(createMockResponse());
            assertThat(userService.getUserByUsername("son").username()).isEqualTo("son");
        }
    }

    @Nested
    class UpdateUserTests {
        @Test
        void updateUser_shouldUpdateAndReturnResponse() {
            User user = createMockUser();
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);
            when(userMapper.toUserResponse(any())).thenReturn(createMockResponse());
            assertThat(userService.updateUser("son", new UpdateUserRequest("n@g.com", "New")).username()).isEqualTo("son");
        }
    }

    @Nested
    class DeleteUserTests {
        @Test
        void deleteUser_shouldDeleteUser_whenUserExists() {
            User user = createMockUser();
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
            userService.deleteUser("son");
            verify(userRepository).delete(user);
        }
    }
}