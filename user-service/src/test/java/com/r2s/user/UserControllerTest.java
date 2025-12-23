package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.user.dto.response.UserResponse;
import com.r2s.core.entity.User;
import com.r2s.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

// === GET /api/users === (ADMIN only)
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        List<UserResponse> mockUsers = List.of(
                new UserResponse(Role.ROLE_ADMIN, "admin@gmail.com", "Admin", "admin"),
                new UserResponse(Role.ROLE_USER, "jane@gmail.com", "Jane Smith", "Jane")
        );

        when(userService.getAllUsers()).thenReturn(mockUsers);

        ResultActions response = mockMvc.perform(get("/api/users"));

        response.andExpect(status().isOk())
// controller tra ve list luon nen khong co object de check message
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("admin"));

        verify(userService).getAllUsers();
    }

// === POST /api/users/create === (ADMIN only)
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserRequest request = new UserRequest("john", "1234",
                "John Doe", "john@gmail.com", Role.ROLE_USER);

        User createdUser = User.builder()
                .username("john")
                .password("1234")
                .name("John Doe")
                .email("john@gmail.com")
                .role(Role.ROLE_USER)
                .build();

        when(userService.createUser(any(UserRequest.class))).thenReturn(createdUser);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));


        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.username").value("john"));

        verify(userService).createUser(any(UserRequest.class));
    }

// === GET /api/users/me ===
    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void getMyProfile_shouldReturnUserProfile() throws Exception {
        UserResponse mockResponse = new UserResponse(Role.ROLE_USER, "user@gmail.com", "User", "user");
        when(userService.getUserByUsername("john")).thenReturn(mockResponse);

        ResultActions response = mockMvc.perform(get("/api/users/me"));

        response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));

        verify(userService).getUserByUsername("john");
    }

// === PUT /api/users/me ===
    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void updateMyProfile_shouldUpdateUser() throws Exception {
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserResponse updated = new UserResponse(Role.ROLE_USER, "john@gmail.com", "Updated Name", "john");

        when(userService.updateUser(eq("john"), any(UpdateUserRequest.class))).thenReturn(updated);

        ResultActions response = mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(userService).updateUser(eq("john"), any(UpdateUserRequest.class));
    }

// === DELETE /api/users/{username} === (ADMIN only)
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteUser_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser("john");

        mockMvc.perform(delete("/api/users/john"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("john");
    }
}