package com.nexus.user.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RatableTransactionTest {

    private final RatableTransaction tx =
            new RatableTransaction("order-1", TransactionType.ORDER, "buyer-1", "seller-1", Instant.now());

    @Test
    void isBetween_acceptsBothDirections() {
        assertThat(tx.isBetween("buyer-1", "seller-1")).isTrue();
        assertThat(tx.isBetween("seller-1", "buyer-1")).isTrue();
    }

    @Test
    void isBetween_rejectsAnyoneElse() {
        assertThat(tx.isBetween("buyer-1", "stranger")).isFalse();
        assertThat(tx.isBetween("stranger", "seller-1")).isFalse();
        assertThat(tx.isBetween("buyer-1", "buyer-1")).isFalse();
        assertThat(tx.isBetween("stranger", "stranger-2")).isFalse();
    }
}
