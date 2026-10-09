package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(String id);
    List<User> findAllNotDeleted();
    boolean existsByRoleId(String roleId);
}
