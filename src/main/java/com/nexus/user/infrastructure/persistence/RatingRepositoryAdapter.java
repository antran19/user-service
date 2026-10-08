package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.RatingRepositoryPort;
import com.nexus.user.domain.model.Rating;
import com.nexus.user.infrastructure.persistence.entity.RatingJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RatingRepositoryAdapter implements RatingRepositoryPort {

    private final RatingJpaRepository jpaRepository;

    public RatingRepositoryAdapter(RatingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Rating save(Rating rating) {
        RatingJpaEntity entity = new RatingJpaEntity(
                UUID.fromString(rating.getId()), rating.getTransactionType().name(), rating.getTransactionId(),
                UUID.fromString(rating.getRaterId()), UUID.fromString(rating.getRatedUserId()), rating.getScore(),
                rating.getComment(), rating.getCreatedAt());
        jpaRepository.save(entity);
        return rating;
    }

    @Override
    public boolean existsByTransactionIdAndRaterId(String transactionId, String raterId) {
        return jpaRepository.existsByTransactionIdAndRaterId(transactionId, UUID.fromString(raterId));
    }
}
