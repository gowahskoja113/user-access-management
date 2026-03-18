package com.r2s.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.r2s.core.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import com.r2s.user.dto.request.UpdateUserRequest;
import com.r2s.user.dto.request.UserRequest;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private Role userRole;

    @BeforeEach
    void setupData() {
        userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_USER)));
        roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_ADMIN)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldSaveToDb_andReturnSuccess() throws Exception {
        UserRequest request = new UserRequest("new_user", "123456", "New User", "new@gmail.com", RoleName.ROLE_USER);
        mockMvc.perform(post("/api/users/create").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        assertTrue(userRepository.existsByUsername("new_user"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_shouldReturn400_whenUsernameExists() throws Exception {
        User existing = User.builder().username("duplicate").password("123").email("e@e.com").name("N").roles(Set.of(userRole)).build();
        userRepository.save(existing);
        UserRequest request = new UserRequest("duplicate", "123", "N", "e2@e.com", RoleName.ROLE_USER);
        mockMvc.perform(post("/api/users/create").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_shouldReturnList() throws Exception {
        userRepository.deleteAll();
        userRepository.save(User.builder().username("u1").password("p").email("e1").name("n1").roles(Set.of(userRole)).build());
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "myuser", roles = "USER")
    void getMyProfile_shouldReturnCorrectInfo() throws Exception {
        userRepository.save(User.builder().username("myuser").password("p").email("m@m.com").name("My Name").roles(Set.of(userRole)).build());
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("My Name"));
    }

    @Test
    @WithMockUser(username = "update_user", roles = "USER")
    void updateMyProfile_shouldChangeDataInDB() throws Exception {
        User testUser = User.builder()
                .username("update_user")
                .password("p")
                .email("o@o.com")
                .name("Old")
                .roles(new HashSet<>(Set.of(userRole)))
                .enabled(true)
                .build();

        userRepository.save(testUser);

        UpdateUserRequest req = new UpdateUserRequest("new@g.com", "New Name");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        assertEquals("New Name", userRepository.findByUsername("update_user").orElseThrow().getName());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_shouldRemoveFromDB() throws Exception {
        userRepository.save(User.builder().username("todelete").password("p").email("d@d.com").name("D").roles(Set.of(userRole)).build());
        mockMvc.perform(delete("/api/users/todelete")).andExpect(status().isOk());
        assertFalse(userRepository.findByUsername("todelete").isPresent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_shouldReturn403_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_shouldReturn401_whenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_returns400_whenPayloadInvalid() throws Exception {
        UserRequest request = new UserRequest("", "123", "N", "not-email", RoleName.ROLE_USER);
        mockMvc.perform(post("/api/users/create").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}