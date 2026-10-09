package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;

import java.util.List;
import java.util.Map;

public class ListUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    public ListUsersUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public List<UserResult> list() {
        // Roles are few (seeded, not user-generated at scale) -- loading them all once and
        // mapping in memory avoids an N+1 findById per user.
        Map<String, String> roleCodeById = roleRepositoryPort.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Role::id, Role::code));

        return userRepositoryPort.findAllNotDeleted().stream()
                .map(user -> UserResult.from(user, roleCodeById.get(user.getRoleId().value())))
                .toList();
    }
}
