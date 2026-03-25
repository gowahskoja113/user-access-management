package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.auth.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.auth.dto.response.UserResponse;
import com.r2s.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "JWT_SECRET=my_super_secret_key_for_unit_testing_only_123456",
        "jwt.expiration=3600000"
})
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userServiceImpl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        Role adminR = new Role(); adminR.setName(RoleName.ROLE_ADMIN);
        Role userR = new Role(); userR.setName(RoleName.ROLE_USER);

        List<UserResponse> mockUsers = List.of(
                new UserResponse(Set.of(adminR), "admin@gmail.com", "Admin", "admin"),
                new UserResponse(Set.of(userR), "jane@gmail.com", "Jane Smith", "Jane")
        );

        when(userServiceImpl.getAllUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].roles[0].name").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.data[1].roles[0].name").value("ROLE_USER"));

        verify(userServiceImpl).getAllUsers();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserRequest request = new UserRequest("john", "1234", "John Doe", "john@gmail.com", RoleName.ROLE_USER);
        Role userR = new Role(); userR.setName(RoleName.ROLE_USER);
        UserResponse response = new UserResponse(Set.of(userR), "john@gmail.com", "John Doe", "john");

        when(userServiceImpl.createUser(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles[0].name").value("ROLE_USER"));

        verify(userServiceImpl).createUser(any(UserRequest.class));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void getMyProfile_shouldReturnUserProfile() throws Exception {
        Role userR = new Role(); userR.setName(RoleName.ROLE_USER);
        UserResponse mockResponse = new UserResponse(Set.of(userR), "john@gmail.com", "User", "john");

        when(userServiceImpl.getUserByUsername("john")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"));
    }

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void updateMyProfile_shouldUpdateUser() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest("updated@example.com", "Updated Name");
        Role userR = new Role(); userR.setName(RoleName.ROLE_USER);
        UserResponse updated = new UserResponse(Set.of(userR), "updated@example.com", "Updated Name", "john");

        when(userServiceImpl.updateUser(eq("john"), any(UpdateUserRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_shouldReturnSuccess() throws Exception {
        doNothing().when(userServiceImpl).deleteUser("john");

        mockMvc.perform(delete("/api/users/john"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUser_returns400_whenPayloadInvalid() throws Exception {
        UserRequest request = new UserRequest("", "", "", "invalid-email", RoleName.ROLE_USER);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
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