package com.nexus.user.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ratings")
public class RatingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "rater_id", nullable = false)
    private UUID raterId;

    @Column(name = "rated_user_id", nullable = false)
    private UUID ratedUserId;

    @Column(nullable = false)
    private int score;

    @Column
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RatingJpaEntity() {
    }

    public RatingJpaEntity(UUID id, String transactionType, String transactionId, UUID raterId, UUID ratedUserId,
                            int score, String comment, Instant createdAt) {
        this.id = id;
        this.transactionType = transactionType;
        this.transactionId = transactionId;
        this.raterId = raterId;
        this.ratedUserId = ratedUserId;
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getTransactionType() { return transactionType; }
    public String getTransactionId() { return transactionId; }
    public UUID getRaterId() { return raterId; }
    public UUID getRatedUserId() { return ratedUserId; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
