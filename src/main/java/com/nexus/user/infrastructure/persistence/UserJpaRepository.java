package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    List<UserJpaEntity> findByDeletedAtIsNull();
    boolean existsByRoleId(UUID roleId);
}
