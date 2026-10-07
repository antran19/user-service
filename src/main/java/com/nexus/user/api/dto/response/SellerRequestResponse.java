package com.nexus.user.api.dto.response;

import java.time.Instant;

public record SellerRequestResponse(String id, String userId, String status, Instant requestedAt,
                                     Instant reviewedAt, String reviewedBy) {
}
