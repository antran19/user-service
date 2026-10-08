package com.nexus.user.api.dto.response;

public record ReputationResponse(String userId, int score, String trustLevel, int totalRatings,
                                  double averageRating) {
}
