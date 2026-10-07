package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ConflictException;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.SellerRequest;
import com.nexus.user.domain.model.SellerRequestStatus;
import com.nexus.user.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

public class ReviewSellerRequestUseCase {

    private static final String SELLER_ROLE_CODE = "SELLER";

    private final SellerRequestRepositoryPort sellerRequestRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    public ReviewSellerRequestUseCase(SellerRequestRepositoryPort sellerRequestRepositoryPort,
                                       UserRepositoryPort userRepositoryPort,
                                       RoleRepositoryPort roleRepositoryPort) {
        this.sellerRequestRepositoryPort = sellerRequestRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Transactional
    public SellerRequestResult review(String requestId, String reviewerId, boolean approve) {
        SellerRequest request = sellerRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new NotFoundException("SELLER_REQUEST_NOT_FOUND",
                        "Seller request not found: " + requestId));

        if (request.getStatus() != SellerRequestStatus.PENDING) {
            throw new ConflictException("SELLER_REQUEST_ALREADY_REVIEWED",
                    "Seller request " + requestId + " was already reviewed");
        }

        if (approve) {
            // Flip the requesting user's role to SELLER first -- if this fails (user deleted,
            // role not seeded), the request stays PENDING instead of being marked APPROVED
            // without the role change actually happening.
            User user = userRepositoryPort.findById(request.getUserId())
                    .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                            "User not found: " + request.getUserId()));
            Role sellerRole = roleRepositoryPort.findByCode(SELLER_ROLE_CODE)
                    .orElseThrow(() -> new IllegalStateException(
                            "Role '" + SELLER_ROLE_CODE + "' is not seeded — check V2 migration"));
            userRepositoryPort.save(user.withRoleId(new RoleId(sellerRole.id())));
        }

        SellerRequest reviewed = approve ? request.approve(reviewerId) : request.reject(reviewerId);
        SellerRequest saved = sellerRequestRepositoryPort.save(reviewed);
        return SellerRequestResult.from(saved);
    }
}
