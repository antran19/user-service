package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetReputationUseCaseTest {

    private ReputationProfileRepositoryPort reputationProfileRepositoryPort;
    private GetReputationUseCase useCase;

    @BeforeEach
    void setUp() {
        reputationProfileRepositoryPort = mock(ReputationProfileRepositoryPort.class);
        useCase = new GetReputationUseCase(reputationProfileRepositoryPort);
    }

    @Test
    void getReputation_returnsDefaultProfileWhenNoneExists() {
        when(reputationProfileRepositoryPort.findByUserId("user-1")).thenReturn(Optional.empty());

        ReputationResult result = useCase.getReputation("user-1");

        assertThat(result.score()).isEqualTo(50);
        assertThat(result.trustLevel()).isEqualTo("TRUSTED");
        assertThat(result.totalRatings()).isEqualTo(0);
    }
}
