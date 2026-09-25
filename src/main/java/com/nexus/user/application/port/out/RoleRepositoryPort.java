package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.Role;

import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByCode(String code);
    Optional<Role> findById(String id);
}
