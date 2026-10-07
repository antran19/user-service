package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ConflictException;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.SellerRequest;
import com.nexus.user.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public class RequestSellerUpgradeUseCase {

    private static final Set<String> ALREADY_SELLING_ROLE_CODES = Set.of("SELLER", "ADMIN");

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final SellerRequestRepositoryPort sellerRequestRepositoryPort;

    public RequestSellerUpgradeUseCase(UserRepositoryPort userRepositoryPort,
                                        RoleRepositoryPort roleRepositoryPort,
                                        SellerRequestRepositoryPort sellerRequestRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.roleRepositoryPort = roleRepositoryPort;
        this.sellerRequestRepositoryPort = sellerRequestRepositoryPort;
    }

    @Transactional
    public SellerRequestResult requestUpgrade(String userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found: " + userId));

        Role currentRole = roleRepositoryPort.findById(user.getRoleId().value())
                .orElseThrow(() -> new IllegalStateException("User's role no longer exists: " + user.getRoleId()));
        if (ALREADY_SELLING_ROLE_CODES.contains(currentRole.code())) {
            throw new ConflictException("ALREADY_SELLER", "User can already create listings: " + userId);
        }

        if (sellerRequestRepositoryPort.findPendingByUserId(userId).isPresent()) {
            throw new ConflictException("SELLER_REQUEST_ALREADY_PENDING",
                    "A seller request is already pending for this user");
        }

        SellerRequest saved = sellerRequestRepositoryPort.save(SellerRequest.create(userId));
        return SellerRequestResult.from(saved);
    }
}
