package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.domain.model.Role;

import java.util.Set;

public class UpdateRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    public UpdateRoleUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public RoleResult update(String roleId, String name, Set<String> privilegeCodes) {
        Role existing = roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found: " + roleId));

        Role updated = new Role(existing.id(), existing.code(), name,
                privilegeCodes == null ? existing.privilegeCodes() : privilegeCodes);
        return RoleResult.from(roleRepositoryPort.save(updated));
    }
}
