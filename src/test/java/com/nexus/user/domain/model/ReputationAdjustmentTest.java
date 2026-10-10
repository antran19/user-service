package com.nexus.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReputationAdjustmentTest {

    @Test
    void create_generatesIdAndStoresFields() {
        ReputationAdjustment adjustment = ReputationAdjustment.create("user-1", "admin-1", -5, "Manual correction");

        assertThat(adjustment.getId()).isNotBlank();
        assertThat(adjustment.getUserId()).isEqualTo("user-1");
        assertThat(adjustment.getAdminId()).isEqualTo("admin-1");
        assertThat(adjustment.getDelta()).isEqualTo(-5);
        assertThat(adjustment.getReason()).isEqualTo("Manual correction");
        assertThat(adjustment.getAppliedAt()).isNotNull();
    }
}
