package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;

public class GetUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    public GetUserUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public UserResult get(String userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));
        if (user.isDeleted()) {
            throw new NotFoundException("USER_NOT_FOUND", "User not found: " + userId);
        }
        Role role = roleRepositoryPort.findById(user.getRoleId().value())
                .orElseThrow(() -> new IllegalStateException("User references a non-existent role: " + user.getRoleId()));
        return UserResult.from(user, role.code());
    }
}
