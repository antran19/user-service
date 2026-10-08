package com.nexus.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReputationProfileTest {

    @Test
    void createDefault_startsAtNeutralFiftyWithTrustLevelTrusted() {
        ReputationProfile profile = ReputationProfile.createDefault("user-1");

        assertThat(profile.getScore()).isEqualTo(50);
        assertThat(profile.getTotalRatings()).isEqualTo(0);
        assertThat(profile.trustLevel()).isEqualTo(TrustLevel.TRUSTED);
    }

    @Test
    void applyRating_fiveStarsIncreasesScoreByFour() {
        ReputationProfile profile = ReputationProfile.createDefault("user-1").applyRating(5);

        assertThat(profile.getScore()).isEqualTo(54);
        assertThat(profile.getTotalRatings()).isEqualTo(1);
        assertThat(profile.averageRating()).isEqualTo(5.0);
    }

    @Test
    void applyRating_oneStarDecreasesScoreByFour() {
        ReputationProfile profile = ReputationProfile.createDefault("user-1").applyRating(1);

        assertThat(profile.getScore()).isEqualTo(46);
    }

    @Test
    void applyRating_threeStarsIsNeutral() {
        ReputationProfile profile = ReputationProfile.createDefault("user-1").applyRating(3);

        assertThat(profile.getScore()).isEqualTo(50);
    }

    @Test
    void applyPenalty_decreasesScoreByPoints() {
        ReputationProfile profile = ReputationProfile.createDefault("user-1").applyPenalty(10);

        assertThat(profile.getScore()).isEqualTo(40);
        assertThat(profile.trustLevel()).isEqualTo(TrustLevel.NORMAL);
    }

    @Test
    void score_neverGoesBelowZero() {
        ReputationProfile profile = ReputationProfile.createDefault("user-1");
        for (int i = 0; i < 20; i++) {
            profile = profile.applyPenalty(10);
        }

        assertThat(profile.getScore()).isEqualTo(0);
        assertThat(profile.trustLevel()).isEqualTo(TrustLevel.LOW);
    }

    @Test
    void score_neverExceedsOneHundred() {
        ReputationProfile profile = ReputationProfile.createDefault("user-1");
        for (int i = 0; i < 20; i++) {
            profile = profile.applyRating(5);
        }

        assertThat(profile.getScore()).isEqualTo(100);
    }

    @Test
    void averageRating_isZeroWithNoRatings() {
        assertThat(ReputationProfile.createDefault("user-1").averageRating()).isEqualTo(0.0);
    }
}
