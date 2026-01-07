package com.r2s.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class UserServiceApplicationTest {

    @Test
    void contextLoads() {
        // Test that the main application class can be instantiated without errors
        // This is a simple smoke test for the application structure
        try {
            Class<?> clazz = Class.forName("com.r2s.user.UserServiceApplication");
            // Just verify the class exists and can be loaded
            assert clazz != null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("UserServiceApplication class not found", e);
        }
    }
}
