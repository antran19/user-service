package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.RoleRepositoryPort;

import java.util.List;

public class ListRolesUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    public ListRolesUseCase(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public List<RoleResult> list() {
        return roleRepositoryPort.findAll().stream().map(RoleResult::from).toList();
    }
}
