package com.nexus.user.application.usecase;

import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.domain.model.SellerRequestStatus;

import java.util.List;

public class ListSellerRequestsUseCase {

    private final SellerRequestRepositoryPort sellerRequestRepositoryPort;

    public ListSellerRequestsUseCase(SellerRequestRepositoryPort sellerRequestRepositoryPort) {
        this.sellerRequestRepositoryPort = sellerRequestRepositoryPort;
    }

    public List<SellerRequestResult> list(SellerRequestStatus status) {
        return sellerRequestRepositoryPort.findByStatus(status).stream()
                .map(SellerRequestResult::from)
                .toList();
    }
}
