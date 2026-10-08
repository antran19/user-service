package com.nexus.user.api.dto.response;

import java.time.Instant;

public record RatingResponse(String id, String transactionType, String transactionId, String raterId,
                              String ratedUserId, int score, String comment, Instant createdAt) {
}
