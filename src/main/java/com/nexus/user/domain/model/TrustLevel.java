package com.nexus.user.domain.model;

// Thresholds match the SRS's MIN_REPUTATION_TO_BID (40) and MIN_REPUTATION_TO_SELL (50)
// configs exactly -- a user needs TRUSTED to create an auction, at least NORMAL to bid.
public enum TrustLevel {
    LOW, NORMAL, TRUSTED;

    public static TrustLevel forScore(int score) {
        if (score < 40) return LOW;
        if (score < 50) return NORMAL;
        return TRUSTED;
    }
}
