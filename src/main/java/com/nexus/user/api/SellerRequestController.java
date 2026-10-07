package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.RequiresPrivilege;
import com.nexus.user.api.dto.response.SellerRequestResponse;
import com.nexus.user.api.mapper.UserApiMapper;
import com.nexus.user.application.usecase.ListSellerRequestsUseCase;
import com.nexus.user.application.usecase.RequestSellerUpgradeUseCase;
import com.nexus.user.application.usecase.ReviewSellerRequestUseCase;
import com.nexus.user.application.usecase.SellerRequestResult;
import com.nexus.user.domain.model.SellerRequestStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class SellerRequestController {

    private final RequestSellerUpgradeUseCase requestSellerUpgradeUseCase;
    private final ListSellerRequestsUseCase listSellerRequestsUseCase;
    private final ReviewSellerRequestUseCase reviewSellerRequestUseCase;
    private final UserApiMapper mapper;

    public SellerRequestController(RequestSellerUpgradeUseCase requestSellerUpgradeUseCase,
                                    ListSellerRequestsUseCase listSellerRequestsUseCase,
                                    ReviewSellerRequestUseCase reviewSellerRequestUseCase,
                                    UserApiMapper mapper) {
        this.requestSellerUpgradeUseCase = requestSellerUpgradeUseCase;
        this.listSellerRequestsUseCase = listSellerRequestsUseCase;
        this.reviewSellerRequestUseCase = reviewSellerRequestUseCase;
        this.mapper = mapper;
    }

    // Deliberately NOT @RequiresPrivilege-gated: any authenticated user (a BUYER by default)
    // may ask to become a seller. ADMIN review is what gates the actual role change.
    @PostMapping("/me/seller-requests")
    public ResponseEntity<ApiResponse<SellerRequestResponse>> requestUpgrade(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        SellerRequestResult result = requestSellerUpgradeUseCase.requestUpgrade(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("USER.REVIEW_SELLER_REQUESTS")
    @GetMapping("/seller-requests")
    public ResponseEntity<ApiResponse<List<SellerRequestResponse>>> list(
            @RequestParam(defaultValue = "PENDING") SellerRequestStatus status) {
        List<SellerRequestResponse> responses = listSellerRequestsUseCase.list(status).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @RequiresPrivilege("USER.REVIEW_SELLER_REQUESTS")
    @PostMapping("/seller-requests/{id}/approve")
    public ResponseEntity<ApiResponse<SellerRequestResponse>> approve(Authentication authentication,
                                                                        @PathVariable String id) {
        String reviewerId = (String) authentication.getPrincipal();
        SellerRequestResult result = reviewSellerRequestUseCase.review(id, reviewerId, true);
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("USER.REVIEW_SELLER_REQUESTS")
    @PostMapping("/seller-requests/{id}/reject")
    public ResponseEntity<ApiResponse<SellerRequestResponse>> reject(Authentication authentication,
                                                                       @PathVariable String id) {
        String reviewerId = (String) authentication.getPrincipal();
        SellerRequestResult result = reviewSellerRequestUseCase.review(id, reviewerId, false);
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(result)));
    }
}
