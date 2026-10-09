package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ConflictException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.domain.model.Role;

import java.util.Set;
import java.util.UUID;

public class CreateRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    public CreateRoleUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public RoleResult create(String code, String name, Set<String> privilegeCodes) {
        if (roleRepositoryPort.findByCode(code).isPresent()) {
            throw new ConflictException("ROLE_CODE_ALREADY_EXISTS", "Role code already exists: " + code);
        }

        Role role = new Role(UUID.randomUUID().toString(), code, name,
                privilegeCodes == null ? Set.of() : privilegeCodes);
        return RoleResult.from(roleRepositoryPort.save(role));
    }
}
