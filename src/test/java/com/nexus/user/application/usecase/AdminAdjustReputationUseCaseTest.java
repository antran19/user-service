package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.application.port.out.ReputationAdjustmentRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.domain.model.ReputationProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminAdjustReputationUseCaseTest {

    private ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private ReputationAdjustmentRepositoryPort reputationAdjustmentRepositoryPort;
    private AdminAdjustReputationUseCase useCase;

    @BeforeEach
    void setUp() {
        reputationProfileRepositoryPort = mock(ReputationProfileRepositoryPort.class);
        reputationAdjustmentRepositoryPort = mock(ReputationAdjustmentRepositoryPort.class);
        useCase = new AdminAdjustReputationUseCase(reputationProfileRepositoryPort, reputationAdjustmentRepositoryPort);
        when(reputationProfileRepositoryPort.save(any(ReputationProfile.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void adjust_appliesPositiveDeltaAndRecordsIt() {
        when(reputationProfileRepositoryPort.findByUserId("user-1"))
                .thenReturn(Optional.of(ReputationProfile.createDefault("user-1")));

        ReputationResult result = useCase.adjust("user-1", "admin-1", 10, "Reward for great seller behavior");

        assertThat(result.score()).isEqualTo(60);
        verify(reputationAdjustmentRepositoryPort).save(argThat(a ->
                a.getUserId().equals("user-1") && a.getAdminId().equals("admin-1") && a.getDelta() == 10
                        && a.getReason().equals("Reward for great seller behavior")));
    }

    @Test
    void adjust_appliesNegativeDelta() {
        when(reputationProfileRepositoryPort.findByUserId("user-1"))
                .thenReturn(Optional.of(ReputationProfile.createDefault("user-1")));

        ReputationResult result = useCase.adjust("user-1", "admin-1", -15, "Manual penalty for abuse report");

        assertThat(result.score()).isEqualTo(35);
    }

    @Test
    void adjust_defaultsToTheNeutralProfileWhenNoneExistsYet() {
        when(reputationProfileRepositoryPort.findByUserId("user-2")).thenReturn(Optional.empty());

        ReputationResult result = useCase.adjust("user-2", "admin-1", 5, "Initial bonus");

        assertThat(result.score()).isEqualTo(55);
    }

    @Test
    void adjust_rejectsZeroDelta() {
        assertThatThrownBy(() -> useCase.adjust("user-1", "admin-1", 0, "No-op"))
                .isInstanceOf(ValidationException.class);

        verify(reputationProfileRepositoryPort, never()).save(any());
        verify(reputationAdjustmentRepositoryPort, never()).save(any());
    }

    @Test
    void adjust_rejectsBlankReason() {
        assertThatThrownBy(() -> useCase.adjust("user-1", "admin-1", 5, "  "))
                .isInstanceOf(ValidationException.class);

        verify(reputationProfileRepositoryPort, never()).save(any());
    }
}
