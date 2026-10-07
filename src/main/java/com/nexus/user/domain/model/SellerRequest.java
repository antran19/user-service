package com.nexus.user.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SellerRequest {
    private final String id;
    private final String userId;
    private final SellerRequestStatus status;
    private final Instant requestedAt;
    private final Instant reviewedAt;
    private final String reviewedBy;

    private SellerRequest(String id, String userId, SellerRequestStatus status, Instant requestedAt,
                           Instant reviewedAt, String reviewedBy) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedBy = reviewedBy;
    }

    public static SellerRequest create(String userId) {
        return new SellerRequest(UUID.randomUUID().toString(), userId, SellerRequestStatus.PENDING,
                Instant.now(), null, null);
    }

    public static SellerRequest reconstitute(String id, String userId, SellerRequestStatus status,
                                              Instant requestedAt, Instant reviewedAt, String reviewedBy) {
        return new SellerRequest(id, userId, status, requestedAt, reviewedAt, reviewedBy);
    }

    public SellerRequest approve(String reviewerId) {
        return new SellerRequest(id, userId, SellerRequestStatus.APPROVED, requestedAt, Instant.now(), reviewerId);
    }

    public SellerRequest reject(String reviewerId) {
        return new SellerRequest(id, userId, SellerRequestStatus.REJECTED, requestedAt, Instant.now(), reviewerId);
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public SellerRequestStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getReviewedBy() { return reviewedBy; }
}
