package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;

// The account behind the token. A soft-deleted account is "not found", like everywhere else here.
public class GetMyProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    public GetMyProfileUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public ProfileResult get(String userId) {
        User user = userRepositoryPort.findById(userId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));
        Role role = roleRepositoryPort.findById(user.getRoleId().value())
                .orElseThrow(() -> new IllegalStateException("User references a non-existent role: " + user.getRoleId()));
        return ProfileResult.from(user, role.code());
    }
}
