package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.SellerRequest;

import java.time.Instant;

public record SellerRequestResult(String id, String userId, String status, Instant requestedAt,
                                   Instant reviewedAt, String reviewedBy) {

    public static SellerRequestResult from(SellerRequest request) {
        return new SellerRequestResult(request.getId(), request.getUserId(), request.getStatus().name(),
                request.getRequestedAt(), request.getReviewedAt(), request.getReviewedBy());
    }
}
