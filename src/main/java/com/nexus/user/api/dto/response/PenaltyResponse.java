package com.nexus.user.api.dto.response;

import java.time.Instant;

public record PenaltyResponse(String id, String userId, String reason, int points, String referenceId,
                               Instant appliedAt) {
}
