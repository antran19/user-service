package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.Role;

import java.util.Set;

public record RoleResult(String id, String code, String name, Set<String> privilegeCodes) {

    public static RoleResult from(Role role) {
        return new RoleResult(role.id(), role.code(), role.name(), role.privilegeCodes());
    }
}
