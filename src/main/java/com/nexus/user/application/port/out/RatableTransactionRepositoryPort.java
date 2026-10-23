package com.nexus.user.application.port.out;

import com.nexus.user.domain.model.RatableTransaction;
import com.nexus.user.domain.model.TransactionType;

import java.util.Optional;

public interface RatableTransactionRepositoryPort {

    // Events are delivered at least once, so recording the same transaction again must be a no-op.
    void insertIfAbsent(RatableTransaction transaction);

    Optional<RatableTransaction> find(String transactionId, TransactionType type);
}
