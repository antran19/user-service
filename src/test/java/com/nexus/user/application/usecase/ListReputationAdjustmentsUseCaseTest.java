package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.ReputationAdjustmentRepositoryPort;
import com.nexus.user.domain.model.ReputationAdjustment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListReputationAdjustmentsUseCaseTest {

    private final ReputationAdjustmentRepositoryPort reputationAdjustmentRepositoryPort =
            mock(ReputationAdjustmentRepositoryPort.class);
    private final ListReputationAdjustmentsUseCase useCase =
            new ListReputationAdjustmentsUseCase(reputationAdjustmentRepositoryPort);

    @Test
    void list_mapsEveryAdjustmentForTheUser() {
        when(reputationAdjustmentRepositoryPort.findByUserId("user-1")).thenReturn(
                List.of(ReputationAdjustment.create("user-1", "admin-1", 5, "Reward")));

        List<AdjustmentResult> results = useCase.list("user-1");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).delta()).isEqualTo(5);
    }
}
