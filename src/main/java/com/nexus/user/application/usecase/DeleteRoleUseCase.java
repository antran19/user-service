package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ConflictException;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;

public class DeleteRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public DeleteRoleUseCase(RoleRepositoryPort roleRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    public void delete(String roleId) {
        roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found: " + roleId));

        if (userRepositoryPort.existsByRoleId(roleId)) {
            throw new ConflictException("ROLE_IN_USE", "Cannot delete a role that is assigned to users: " + roleId);
        }

        roleRepositoryPort.deleteById(roleId);
    }
}
