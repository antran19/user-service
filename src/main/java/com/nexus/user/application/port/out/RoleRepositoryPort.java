package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByCode(String code);
    Optional<Role> findById(String id);
    List<Role> findAll();
    Role save(Role role);
    void deleteById(String id);
}
