package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.ReputationAdjustmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReputationAdjustmentJpaRepository extends JpaRepository<ReputationAdjustmentJpaEntity, UUID> {
    List<ReputationAdjustmentJpaEntity> findByUserId(UUID userId);
}
