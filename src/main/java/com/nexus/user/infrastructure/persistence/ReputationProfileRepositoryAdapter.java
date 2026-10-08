package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.domain.model.ReputationProfile;
import com.nexus.user.infrastructure.persistence.entity.ReputationProfileJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ReputationProfileRepositoryAdapter implements ReputationProfileRepositoryPort {

    private final ReputationProfileJpaRepository jpaRepository;

    public ReputationProfileRepositoryAdapter(ReputationProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ReputationProfile> findByUserId(String userId) {
        return jpaRepository.findById(UUID.fromString(userId)).map(this::toDomain);
    }

    @Override
    public ReputationProfile save(ReputationProfile profile) {
        ReputationProfileJpaEntity entity = new ReputationProfileJpaEntity(
                UUID.fromString(profile.getUserId()), profile.getScore(), profile.getTotalRatings(),
                profile.getRatingSum(), profile.getUpdatedAt());
        jpaRepository.save(entity);
        return profile;
    }

    private ReputationProfile toDomain(ReputationProfileJpaEntity entity) {
        return ReputationProfile.reconstitute(entity.getUserId().toString(), entity.getScore(),
                entity.getTotalRatings(), entity.getRatingSum(), entity.getUpdatedAt());
    }
}
