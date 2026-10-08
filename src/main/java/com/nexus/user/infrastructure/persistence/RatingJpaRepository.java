package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.RatingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RatingJpaRepository extends JpaRepository<RatingJpaEntity, UUID> {
    boolean existsByTransactionIdAndRaterId(String transactionId, UUID raterId);
}
