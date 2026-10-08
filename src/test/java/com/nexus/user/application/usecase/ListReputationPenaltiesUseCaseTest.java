package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.ReputationPenaltyRepositoryPort;
import com.nexus.user.domain.model.ReputationPenalty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListReputationPenaltiesUseCaseTest {

    @Test
    void list_returnsPenaltiesForUser() {
        ReputationPenaltyRepositoryPort port = mock(ReputationPenaltyRepositoryPort.class);
        when(port.findByUserId("user-1")).thenReturn(
                List.of(ReputationPenalty.create("user-1", "AUCTION_PAYMENT_TIMEOUT", 10, "auction-1")));

        List<PenaltyResult> result = new ListReputationPenaltiesUseCase(port).list("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).points()).isEqualTo(10);
    }
}
