package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.ReputationProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReputationProfileJpaRepository extends JpaRepository<ReputationProfileJpaEntity, UUID> {
}
