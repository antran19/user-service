package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.ReputationProfile;

public record ReputationResult(String userId, int score, String trustLevel, int totalRatings, double averageRating) {

    public static ReputationResult from(ReputationProfile profile) {
        return new ReputationResult(profile.getUserId(), profile.getScore(), profile.trustLevel().name(),
                profile.getTotalRatings(), profile.averageRating());
    }
}
