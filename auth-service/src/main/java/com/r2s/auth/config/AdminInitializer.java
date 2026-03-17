package com.r2s.auth.config;

import com.r2s.core.entity.Role;
import com.r2s.core.entity.RoleName;
import com.r2s.core.entity.User;
import com.r2s.core.repository.RoleRepository;
import com.r2s.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("admin").isEmpty()) {

            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("12345678"))
                    .email("admin@gmail.com")
                    .name("Admin System")
                    .roles(new HashSet<>())
                    .build();

            admin.getRoles().add(adminRole);

            userRepository.save(admin);
        }
    }
}