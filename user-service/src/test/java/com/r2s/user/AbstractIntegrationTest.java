package com.r2s.user;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

public abstract class AbstractIntegrationTest {

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        // Use the docker-compose postgres container
        registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5432/auth-service");
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "Passw0rd");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
