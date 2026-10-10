package com.nexus.user.domain.model;

import java.time.Instant;
import java.util.UUID;

// A manual admin override (USER.REPUTATION.ADJUST), distinct from ReputationPenalty: a
// penalty is always a fixed-size automatic consequence of a system event (e.g. auction
// payment timeout); an adjustment is a signed, admin-chosen delta with a free-text reason,
// and always records who made it.
public class ReputationAdjustment {

    private final String id;
    private final String userId;
    private final String adminId;
    private final int delta;
    private final String reason;
    private final Instant appliedAt;

    private ReputationAdjustment(String id, String userId, String adminId, int delta, String reason,
                                  Instant appliedAt) {
        this.id = id;
        this.userId = userId;
        this.adminId = adminId;
        this.delta = delta;
        this.reason = reason;
        this.appliedAt = appliedAt;
    }

    public static ReputationAdjustment create(String userId, String adminId, int delta, String reason) {
        return new ReputationAdjustment(UUID.randomUUID().toString(), userId, adminId, delta, reason, Instant.now());
    }

    public static ReputationAdjustment reconstitute(String id, String userId, String adminId, int delta,
                                                      String reason, Instant appliedAt) {
        return new ReputationAdjustment(id, userId, adminId, delta, reason, appliedAt);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getAdminId() { return adminId; }
    public int getDelta() { return delta; }
    public String getReason() { return reason; }
    public Instant getAppliedAt() { return appliedAt; }
}
