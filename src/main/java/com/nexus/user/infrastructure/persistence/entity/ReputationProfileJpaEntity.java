package com.nexus.user.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reputation_profiles")
public class ReputationProfileJpaEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private int score;

    @Column(name = "total_ratings", nullable = false)
    private int totalRatings;

    @Column(name = "rating_sum", nullable = false)
    private int ratingSum;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ReputationProfileJpaEntity() {
    }

    public ReputationProfileJpaEntity(UUID userId, int score, int totalRatings, int ratingSum, Instant updatedAt) {
        this.userId = userId;
        this.score = score;
        this.totalRatings = totalRatings;
        this.ratingSum = ratingSum;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() { return userId; }
    public int getScore() { return score; }
    public int getTotalRatings() { return totalRatings; }
    public int getRatingSum() { return ratingSum; }
    public Instant getUpdatedAt() { return updatedAt; }
}
