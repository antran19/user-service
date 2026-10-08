package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.ReputationPenaltyRepositoryPort;
import com.nexus.user.domain.model.ReputationPenalty;
import com.nexus.user.infrastructure.persistence.entity.ReputationPenaltyJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ReputationPenaltyRepositoryAdapter implements ReputationPenaltyRepositoryPort {

    private final ReputationPenaltyJpaRepository jpaRepository;

    public ReputationPenaltyRepositoryAdapter(ReputationPenaltyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReputationPenalty save(ReputationPenalty penalty) {
        ReputationPenaltyJpaEntity entity = new ReputationPenaltyJpaEntity(
                UUID.fromString(penalty.getId()), UUID.fromString(penalty.getUserId()), penalty.getReason(),
                penalty.getPoints(), penalty.getReferenceId(), penalty.getAppliedAt());
        jpaRepository.save(entity);
        return penalty;
    }

    @Override
    public boolean existsByReferenceIdAndReason(String referenceId, String reason) {
        return jpaRepository.existsByReferenceIdAndReason(referenceId, reason);
    }

    @Override
    public List<ReputationPenalty> findByUserId(String userId) {
        return jpaRepository.findByUserId(UUID.fromString(userId)).stream().map(this::toDomain).toList();
    }

    private ReputationPenalty toDomain(ReputationPenaltyJpaEntity entity) {
        return ReputationPenalty.reconstitute(entity.getId().toString(), entity.getUserId().toString(),
                entity.getReason(), entity.getPoints(), entity.getReferenceId(), entity.getAppliedAt());
    }
}
