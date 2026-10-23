package com.nexus.user.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "ratable_transactions")
@IdClass(RatableTransactionJpaEntity.Key.class)
public class RatableTransactionJpaEntity {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Id
    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "buyer_id", nullable = false)
    private String buyerId;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected RatableTransactionJpaEntity() {
    }

    public String getTransactionId() { return transactionId; }
    public String getTransactionType() { return transactionType; }
    public String getBuyerId() { return buyerId; }
    public String getSellerId() { return sellerId; }
    public Instant getCompletedAt() { return completedAt; }

    public static class Key implements Serializable {
        private String transactionId;
        private String transactionType;

        public Key() {
        }

        public Key(String transactionId, String transactionType) {
            this.transactionId = transactionId;
            this.transactionType = transactionType;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Key k && Objects.equals(transactionId, k.transactionId)
                    && Objects.equals(transactionType, k.transactionType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(transactionId, transactionType);
        }
    }
}
