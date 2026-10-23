package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.RatableTransactionRepositoryPort;
import com.nexus.user.domain.model.RatableTransaction;
import com.nexus.user.domain.model.TransactionType;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

// Called when commerce-service reports an order as shipped. Idempotent: the same event delivered
// twice leaves one row.
public class RecordRatableTransactionUseCase {

    private final RatableTransactionRepositoryPort ratableTransactionRepositoryPort;

    public RecordRatableTransactionUseCase(RatableTransactionRepositoryPort ratableTransactionRepositoryPort) {
        this.ratableTransactionRepositoryPort = ratableTransactionRepositoryPort;
    }

    @Transactional
    public void record(String transactionId, TransactionType type, String buyerId, String sellerId, Instant completedAt) {
        ratableTransactionRepositoryPort.insertIfAbsent(
                new RatableTransaction(transactionId, type, buyerId, sellerId, completedAt));
    }
}
