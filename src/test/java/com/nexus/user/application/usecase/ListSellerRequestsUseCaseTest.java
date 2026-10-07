package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.domain.model.SellerRequest;
import com.nexus.user.domain.model.SellerRequestStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListSellerRequestsUseCaseTest {

    @Test
    void list_returnsRequestsWithGivenStatus() {
        SellerRequestRepositoryPort port = mock(SellerRequestRepositoryPort.class);
        SellerRequest pending = SellerRequest.create("user-1");
        when(port.findByStatus(SellerRequestStatus.PENDING)).thenReturn(List.of(pending));

        ListSellerRequestsUseCase useCase = new ListSellerRequestsUseCase(port);
        List<SellerRequestResult> results = useCase.list(SellerRequestStatus.PENDING);

        assertThat(results).extracting(SellerRequestResult::userId).containsExactly("user-1");
    }
}
