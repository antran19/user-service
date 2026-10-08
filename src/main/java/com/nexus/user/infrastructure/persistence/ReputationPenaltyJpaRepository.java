package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.ReputationPenaltyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReputationPenaltyJpaRepository extends JpaRepository<ReputationPenaltyJpaEntity, UUID> {
    boolean existsByReferenceIdAndReason(String referenceId, String reason);
    List<ReputationPenaltyJpaEntity> findByUserId(UUID userId);
}
