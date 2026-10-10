package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.ReputationAdjustmentRepositoryPort;
import com.nexus.user.domain.model.ReputationAdjustment;
import com.nexus.user.infrastructure.persistence.entity.ReputationAdjustmentJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ReputationAdjustmentRepositoryAdapter implements ReputationAdjustmentRepositoryPort {

    private final ReputationAdjustmentJpaRepository jpaRepository;

    public ReputationAdjustmentRepositoryAdapter(ReputationAdjustmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ReputationAdjustment save(ReputationAdjustment adjustment) {
        ReputationAdjustmentJpaEntity entity = new ReputationAdjustmentJpaEntity(
                UUID.fromString(adjustment.getId()), UUID.fromString(adjustment.getUserId()),
                UUID.fromString(adjustment.getAdminId()), adjustment.getDelta(), adjustment.getReason(),
                adjustment.getAppliedAt());
        jpaRepository.save(entity);
        return adjustment;
    }

    @Override
    public List<ReputationAdjustment> findByUserId(String userId) {
        return jpaRepository.findByUserId(UUID.fromString(userId)).stream().map(this::toDomain).toList();
    }

    private ReputationAdjustment toDomain(ReputationAdjustmentJpaEntity entity) {
        return ReputationAdjustment.reconstitute(entity.getId().toString(), entity.getUserId().toString(),
                entity.getAdminId().toString(), entity.getDelta(), entity.getReason(), entity.getAppliedAt());
    }
}
