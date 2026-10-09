package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

public class UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    public UpdateUserUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Transactional
    public UserResult update(String userId, String fullName, String roleCode) {
        User user = findActiveUser(userId);

        Role role = roleRepositoryPort.findByCode(roleCode)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found: " + roleCode));

        User updated = user.withFullName(fullName).withRoleId(new RoleId(role.id()));
        User saved = userRepositoryPort.save(updated);

        return UserResult.from(saved, role.code());
    }

    // A soft-deleted user is treated as not found -- same convention as "entity does not
    // exist" everywhere else in this service.
    private User findActiveUser(String userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));
        if (user.isDeleted()) {
            throw new NotFoundException("USER_NOT_FOUND", "User not found: " + userId);
        }
        return user;
    }
}
