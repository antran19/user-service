package com.nexus.user.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reputation_adjustments")
public class ReputationAdjustmentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(nullable = false)
    private int delta;

    @Column(nullable = false)
    private String reason;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    protected ReputationAdjustmentJpaEntity() {
    }

    public ReputationAdjustmentJpaEntity(UUID id, UUID userId, UUID adminId, int delta, String reason,
                                          Instant appliedAt) {
        this.id = id;
        this.userId = userId;
        this.adminId = adminId;
        this.delta = delta;
        this.reason = reason;
        this.appliedAt = appliedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getAdminId() { return adminId; }
    public int getDelta() { return delta; }
    public String getReason() { return reason; }
    public Instant getAppliedAt() { return appliedAt; }
}
