package com.nexus.user.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ReputationPenalty {

    private final String id;
    private final String userId;
    private final String reason;
    private final int points;
    private final String referenceId;
    private final Instant appliedAt;

    private ReputationPenalty(String id, String userId, String reason, int points, String referenceId,
                               Instant appliedAt) {
        this.id = id;
        this.userId = userId;
        this.reason = reason;
        this.points = points;
        this.referenceId = referenceId;
        this.appliedAt = appliedAt;
    }

    public static ReputationPenalty create(String userId, String reason, int points, String referenceId) {
        return new ReputationPenalty(UUID.randomUUID().toString(), userId, reason, points, referenceId, Instant.now());
    }

    public static ReputationPenalty reconstitute(String id, String userId, String reason, int points,
                                                  String referenceId, Instant appliedAt) {
        return new ReputationPenalty(id, userId, reason, points, referenceId, appliedAt);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getReason() { return reason; }
    public int getPoints() { return points; }
    public String getReferenceId() { return referenceId; }
    public Instant getAppliedAt() { return appliedAt; }
}
