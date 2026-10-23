package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.RatableTransactionRepositoryPort;
import com.nexus.user.domain.model.RatableTransaction;
import com.nexus.user.domain.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RecordRatableTransactionUseCaseTest {

    private final RatableTransactionRepositoryPort port = mock(RatableTransactionRepositoryPort.class);
    private final RecordRatableTransactionUseCase useCase = new RecordRatableTransactionUseCase(port);

    @Test
    void record_storesTheTransactionWithItsTwoParties() {
        Instant when = Instant.parse("2026-10-23T10:00:00Z");

        useCase.record("order-1", TransactionType.ORDER, "buyer-1", "seller-1", when);

        verify(port).insertIfAbsent(new RatableTransaction("order-1", TransactionType.ORDER, "buyer-1", "seller-1", when));
    }
}
