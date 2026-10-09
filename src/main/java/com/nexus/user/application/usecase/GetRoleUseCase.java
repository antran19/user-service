package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;

public class GetRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    public GetRoleUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public RoleResult get(String roleId) {
        return RoleResult.from(roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new NotFoundException("ROLE_NOT_FOUND", "Role not found: " + roleId)));
    }
}
