package com.nexus.user.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reputation_penalties")
public class ReputationPenaltyJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private int points;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    protected ReputationPenaltyJpaEntity() {
    }

    public ReputationPenaltyJpaEntity(UUID id, UUID userId, String reason, int points, String referenceId,
                                       Instant appliedAt) {
        this.id = id;
        this.userId = userId;
        this.reason = reason;
        this.points = points;
        this.referenceId = referenceId;
        this.appliedAt = appliedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getReason() { return reason; }
    public int getPoints() { return points; }
    public String getReferenceId() { return referenceId; }
    public Instant getAppliedAt() { return appliedAt; }
}
