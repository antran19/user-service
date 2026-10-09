package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.BlacklistedTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenJpaRepository extends JpaRepository<BlacklistedTokenJpaEntity, String> {
}
