package com.nexus.user.domain.model;

import java.time.Instant;

public class ReputationProfile {

    private static final int DEFAULT_SCORE = 50;
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;

    private final String userId;
    private final int score;
    private final int totalRatings;
    private final int ratingSum;
    private final Instant updatedAt;

    private ReputationProfile(String userId, int score, int totalRatings, int ratingSum, Instant updatedAt) {
        this.userId = userId;
        this.score = score;
        this.totalRatings = totalRatings;
        this.ratingSum = ratingSum;
        this.updatedAt = updatedAt;
    }

    public static ReputationProfile createDefault(String userId) {
        return new ReputationProfile(userId, DEFAULT_SCORE, 0, 0, Instant.now());
    }

    public static ReputationProfile reconstitute(String userId, int score, int totalRatings, int ratingSum,
                                                  Instant updatedAt) {
        return new ReputationProfile(userId, score, totalRatings, ratingSum, updatedAt);
    }

    // A 3-star rating is neutral (no change); each star above/below moves the score by 2,
    // so a 5-star rating is worth +4 and a 1-star rating is worth -4.
    public ReputationProfile applyRating(int stars) {
        int delta = (stars - 3) * 2;
        return new ReputationProfile(userId, clamp(score + delta), totalRatings + 1, ratingSum + stars, Instant.now());
    }

    public ReputationProfile applyPenalty(int points) {
        return new ReputationProfile(userId, clamp(score - points), totalRatings, ratingSum, Instant.now());
    }

    // Admin manual adjustment (USER.REPUTATION.ADJUST): unlike applyPenalty, delta is signed
    // -- positive raises the score, negative lowers it -- since an admin override can go
    // either way, not just downward like an automatic penalty.
    public ReputationProfile adjust(int delta) {
        return new ReputationProfile(userId, clamp(score + delta), totalRatings, ratingSum, Instant.now());
    }

    private static int clamp(int value) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, value));
    }

    public TrustLevel trustLevel() {
        return TrustLevel.forScore(score);
    }

    public double averageRating() {
        return totalRatings == 0 ? 0.0 : (double) ratingSum / totalRatings;
    }

    public String getUserId() { return userId; }
    public int getScore() { return score; }
    public int getTotalRatings() { return totalRatings; }
    public int getRatingSum() { return ratingSum; }
    public Instant getUpdatedAt() { return updatedAt; }
}
