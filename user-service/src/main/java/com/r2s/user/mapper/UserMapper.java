package com.r2s.user.mapper;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.user.dto.request.UserRequest;
import com.r2s.core.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final RoleRepository roleRepository;

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        Set<RoleName> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getRoles(),
                user.getEmail(),
                user.getName(),
                user.getUsername()
        );
    }

    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.username());
        user.setName(request.fullName());
        user.setEmail(request.email());

        if (request.roleName() != null) {
            Role role = roleRepository.findByName(request.roleName())
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            user.setRoles(Set.of(role));
        }

        return user;
    }
}