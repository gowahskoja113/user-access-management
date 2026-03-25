package com.r2s.auth.repository;

import com.r2s.auth.entity.Role;
import com.r2s.core.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleName name);
}