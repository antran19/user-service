package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.infrastructure.persistence.entity.PrivilegeJpaEntity;
import com.nexus.user.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository roleJpaRepository;

    public RoleRepositoryAdapter(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return roleJpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public Optional<Role> findById(String id) {
        return roleJpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    private Role toDomain(RoleJpaEntity entity) {
        Set<String> privilegeCodes = entity.getPrivileges().stream()
                .map(PrivilegeJpaEntity::getCode)
                .collect(Collectors.toSet());
        return new Role(entity.getId().toString(), entity.getCode(), entity.getName(), privilegeCodes);
    }
}
