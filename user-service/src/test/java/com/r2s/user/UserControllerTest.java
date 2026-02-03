package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;
import com.r2s.user.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userServiceImpl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // === GET /api/users ===
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        List<UserResponse> mockUsers = List.of(
                new UserResponse(Role.ROLE_ADMIN, "admin@gmail.com", "Admin", "admin"),
                new UserResponse(Role.ROLE_USER, "jane@gmail.com", "Jane Smith", "Jane")
        );

        when(userServiceImpl.getAllUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[0].email").value("admin@gmail.com"))
                .andExpect(jsonPath("$.data[0].role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data[1].username").value("Jane"))
                .andExpect(jsonPath("$.data[1].email").value("jane@gmail.com"))
                .andExpect(jsonPath("$.data[1].role").value("ROLE_USER"));

        verify(userServiceImpl).getAllUsers();
    }

    // === POST /api/users/create ===
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserRequest request = new UserRequest("john", "1234", "John Doe", "john@gmail.com", Role.ROLE_USER);

        UserResponse createdUserResponse = new UserResponse(Role.ROLE_USER, "john@gmail.com", "John Doe", "john");

        when(userServiceImpl.createUser(any(UserRequest.class))).thenReturn(createdUserResponse);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.email").value("john@gmail.com"))
                .andExpect(jsonPath("$.data.role").value("ROLE_USER"));

        verify(userServiceImpl).createUser(any(UserRequest.class));
    }

    // === GET /api/users/me ===
    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void getMyProfile_shouldReturnUserProfile() throws Exception {
        UserResponse mockResponse = new UserResponse(Role.ROLE_USER, "john@gmail.com", "User", "john");

        when(userServiceImpl.getUserByUsername("john")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.email").value("john@gmail.com"));

        verify(userServiceImpl).getUserByUsername("john");
    }

    // === PUT /api/users/me ===
    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void updateMyProfile_shouldUpdateUser() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest("updated@example.com", "Updated Name");
        UserResponse updated = new UserResponse(Role.ROLE_USER, "updated@example.com", "Updated Name", "john");

        when(userServiceImpl.updateUser(eq("john"), any(UpdateUserRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Name"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));

        verify(userServiceImpl).updateUser(eq("john"), any(UpdateUserRequest.class));
    }

    // === DELETE /api/users/{username} ===
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnSuccess() throws Exception {
        doNothing().when(userServiceImpl).deleteUser("john");

        mockMvc.perform(delete("/api/users/john"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userServiceImpl).deleteUser("john");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_returns400_whenPayloadInvalid() throws Exception {
        // send empty username and invalid email
        UserRequest request = new UserRequest("", "", "", "invalid-email", Role.ROLE_USER);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value(notNullValue()))
                .andExpect(jsonPath("$.email").value(notNullValue()));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void updateMyProfile_returns400_whenPayloadInvalid() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("invalid-email", "");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}