package com.nexus.user.infrastructure.persistence;

import com.nexus.user.infrastructure.persistence.entity.RatableTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface RatableTransactionJpaRepository
        extends JpaRepository<RatableTransactionJpaEntity, RatableTransactionJpaEntity.Key> {

    // Atomic "insert if missing": two deliveries of the same event, or the same transaction arriving
    // from two paths, cannot fail on the primary key.
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO ratable_transactions (transaction_id, transaction_type, buyer_id, seller_id, completed_at)
            VALUES (:transactionId, :type, :buyerId, :sellerId, :completedAt)
            ON CONFLICT (transaction_id, transaction_type) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("transactionId") String transactionId, @Param("type") String type,
                       @Param("buyerId") String buyerId, @Param("sellerId") String sellerId,
                       @Param("completedAt") Instant completedAt);
}
