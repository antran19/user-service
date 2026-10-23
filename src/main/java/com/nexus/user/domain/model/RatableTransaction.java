package com.nexus.user.domain.model;

import java.time.Instant;

// A transaction two users may rate each other for: the buyer and the seller of a shipped order
// (or of the auction behind it). Only these two parties can rate it.
public record RatableTransaction(String transactionId, TransactionType type, String buyerId, String sellerId,
                                 Instant completedAt) {

    public boolean isBetween(String userA, String userB) {
        return (buyerId.equals(userA) && sellerId.equals(userB)) || (sellerId.equals(userA) && buyerId.equals(userB));
    }
}
