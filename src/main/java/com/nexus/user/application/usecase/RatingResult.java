package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.Rating;

import java.time.Instant;

public record RatingResult(String id, String transactionType, String transactionId, String raterId,
                            String ratedUserId, int score, String comment, Instant createdAt) {

    public static RatingResult from(Rating rating) {
        return new RatingResult(rating.getId(), rating.getTransactionType().name(), rating.getTransactionId(),
                rating.getRaterId(), rating.getRatedUserId(), rating.getScore(), rating.getComment(),
                rating.getCreatedAt());
    }
}
