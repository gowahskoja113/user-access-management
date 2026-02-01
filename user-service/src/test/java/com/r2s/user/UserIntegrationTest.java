package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.User;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    // Test POST /api/users/create
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldSaveToDb_andReturnSuccess() throws Exception {
        UserRequest request = new UserRequest("new_user", "123456", "New User", "new@gmail.com", Role.ROLE_USER);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("new_user"));

        User savedUser = userRepository.findByUsername("new_user").orElseThrow();
        assertEquals("new@gmail.com", savedUser.getEmail());
    }

    // Test POST /api/users/create with duplicate username
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn400_whenUsernameExists() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("duplicate");
        existingUser.setPassword(passwordEncoder.encode("1234"));
        existingUser.setEmail("exist@gmail.com");
        existingUser.setName("User Mau");
        existingUser.setRole(Role.ROLE_USER);
        userRepository.save(existingUser);

        UserRequest request = new UserRequest("duplicate", "123456", "Any Name", "any@gmail.com", Role.ROLE_USER);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    // Test GET /api/users
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnList() throws Exception {
        // dọn db trước khi test
        userRepository.deleteAll();

        // GIVEN: Seed 2 users vào DB
        User user1 = new User();
        user1.setUsername("u1");
        user1.setPassword("p");
        user1.setName("n1");
        user1.setEmail("e1");
        user1.setRole(Role.ROLE_USER);
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("u2");
        user2.setPassword("p");
        user2.setName("n2");
        user2.setEmail("e2");
        user2.setRole(Role.ROLE_USER);
        userRepository.save(user2);

        // WHEN & THEN
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("u1"))
                .andExpect(jsonPath("$[0].email").value("e1"))
                .andExpect(jsonPath("$[0].role").value("ROLE_USER"))
                .andExpect(jsonPath("$[1].username").value("u2"))
                .andExpect(jsonPath("$[1].email").value("e2"))
                .andExpect(jsonPath("$[1].role").value("ROLE_USER"));
    }

    // Test GET /api/users/me
    @Test
    @WithMockUser(username = "myuser", roles = "USER")
    void getMyProfile_shouldReturnCorrectInfo() throws Exception {
        // GIVEN
        User me = new User();
        me.setUsername("myuser");
        me.setPassword("pass");
        me.setName("My Name");
        me.setEmail("my@gmail.com");
        me.setRole(Role.ROLE_USER);
        userRepository.save(me);

        // WHEN & THEN
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("myuser"))
                .andExpect(jsonPath("$.name").value("My Name"));
    }

    // Test PUT /api/users/me
    @Test
    @WithMockUser(username = "update_user", roles = "USER")
    void updateMyProfile_shouldChangeDataInDB() throws Exception {
        // GIVEN
        User original = new User();
        original.setUsername("update_user");
        original.setPassword("pass");
        original.setName("Old Name");
        original.setEmail("old@gmail.com");
        original.setRole(Role.ROLE_USER);
        userRepository.save(original);

        UpdateUserRequest updateRequest = new UpdateUserRequest("new@gmail.com", "New Name");

        // WHEN
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));

        // THEN: Check Db
        User updated = userRepository.findByUsername("update_user").orElseThrow();
        assertEquals("New Name", updated.getName());
        assertEquals("new@gmail.com", updated.getEmail());
    }

    //Test DELETE /api/users/{username}
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldRemoveFromDB() throws Exception {
        // GIVEN
        User toDelete = new User();
        toDelete.setUsername("todelete");
        toDelete.setPassword("pass");
        toDelete.setName("Del");
        toDelete.setEmail("del@gmail.com");
        toDelete.setRole(Role.ROLE_USER);
        userRepository.save(toDelete);

        // WHEN
        mockMvc.perform(delete("/api/users/todelete"))
                .andExpect(status().isNoContent());

        // THEN: find in DB
        assertFalse(userRepository.findByUsername("todelete").isPresent());
    }

    // Authorization tests
    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturn403_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // 401 when not logged in
    @Test
    void getAllUsers_shouldReturn401_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // Validation tests
    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_returns400_whenPayloadInvalid() throws Exception {
        // Request lacking username and invalid email
        UserRequest request = new UserRequest("", "123456", "Name", "not-an-email", Role.ROLE_USER);

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value(notNullValue()))
                .andExpect(jsonPath("$.email").value(notNullValue()));
    }
}