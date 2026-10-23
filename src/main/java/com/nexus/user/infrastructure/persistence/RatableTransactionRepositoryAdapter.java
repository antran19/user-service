package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.RatableTransactionRepositoryPort;
import com.nexus.user.domain.model.RatableTransaction;
import com.nexus.user.domain.model.TransactionType;
import com.nexus.user.infrastructure.persistence.entity.RatableTransactionJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RatableTransactionRepositoryAdapter implements RatableTransactionRepositoryPort {

    private final RatableTransactionJpaRepository jpaRepository;

    public RatableTransactionRepositoryAdapter(RatableTransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void insertIfAbsent(RatableTransaction t) {
        jpaRepository.insertIfAbsent(t.transactionId(), t.type().name(), t.buyerId(), t.sellerId(), t.completedAt());
    }

    @Override
    public Optional<RatableTransaction> find(String transactionId, TransactionType type) {
        return jpaRepository.findById(new RatableTransactionJpaEntity.Key(transactionId, type.name()))
                .map(e -> new RatableTransaction(e.getTransactionId(), TransactionType.valueOf(e.getTransactionType()),
                        e.getBuyerId(), e.getSellerId(), e.getCompletedAt()));
    }
}
