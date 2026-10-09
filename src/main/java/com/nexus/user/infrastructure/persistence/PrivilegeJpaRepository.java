package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.PrivilegeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;
import java.util.UUID;

public interface PrivilegeJpaRepository extends JpaRepository<PrivilegeJpaEntity, UUID> {
    Set<PrivilegeJpaEntity> findByCodeIn(Set<String> codes);
}
