//package com.r2s.auth.config;
//
//import com.r2s.core.entity.RoleName;
//import com.r2s.auth.entity.User;
//import com.r2s.auth.repository.RoleRepository;
//import com.r2s.auth.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//@Profile("!test")
//public class AdminInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final RoleRepository roleRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//        log.info("Checking for system admin account...");
//
//        if (userRepository.findByUsername("admin").isEmpty()) {
//
//            roleRepository.findByName(RoleName.ROLE_ADMIN).ifPresentOrElse(
//                    adminRole -> {
//                        User admin = User.builder()
//                                .username("admin")
//                                .password(passwordEncoder.encode("12345678"))
//                                .email("admin@gmail.com.vn")
//                                .name("System Administrator")
//                                .enabled(true)
//                                .roles(new HashSet<>(Set.of(adminRole)))
//                                .build();
//
//                        userRepository.save(admin);
//                        log.info("Default Admin account created successfully.");
//                    },
//                    () -> log.warn("Role ROLE_ADMIN not found in database. Admin initialization skipped.")
//            );
//        } else {
//            log.info("Admin account already exists.");
//        }
//    }
//}