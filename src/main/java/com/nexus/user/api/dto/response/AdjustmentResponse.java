package com.nexus.user.api.dto.response;

import java.time.Instant;

public record AdjustmentResponse(String id, String userId, String adminId, int delta, String reason,
                                  Instant appliedAt) {
}
