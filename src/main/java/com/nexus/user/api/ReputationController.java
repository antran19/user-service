package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.RequiresPrivilege;
import com.nexus.user.api.dto.request.AdjustReputationRequest;
import com.nexus.user.api.dto.response.AdjustmentResponse;
import com.nexus.user.api.dto.response.PenaltyResponse;
import com.nexus.user.api.dto.response.ReputationResponse;
import com.nexus.user.api.mapper.UserApiMapper;
import com.nexus.user.application.usecase.AdminAdjustReputationUseCase;
import com.nexus.user.application.usecase.GetReputationUseCase;
import com.nexus.user.application.usecase.ListReputationAdjustmentsUseCase;
import com.nexus.user.application.usecase.ListReputationPenaltiesUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/reputation")
public class ReputationController {

    private final GetReputationUseCase getReputationUseCase;
    private final ListReputationPenaltiesUseCase listReputationPenaltiesUseCase;
    private final AdminAdjustReputationUseCase adminAdjustReputationUseCase;
    private final ListReputationAdjustmentsUseCase listReputationAdjustmentsUseCase;
    private final UserApiMapper mapper;

    public ReputationController(GetReputationUseCase getReputationUseCase,
                                 ListReputationPenaltiesUseCase listReputationPenaltiesUseCase,
                                 AdminAdjustReputationUseCase adminAdjustReputationUseCase,
                                 ListReputationAdjustmentsUseCase listReputationAdjustmentsUseCase,
                                 UserApiMapper mapper) {
        this.getReputationUseCase = getReputationUseCase;
        this.listReputationPenaltiesUseCase = listReputationPenaltiesUseCase;
        this.adminAdjustReputationUseCase = adminAdjustReputationUseCase;
        this.listReputationAdjustmentsUseCase = listReputationAdjustmentsUseCase;
        this.mapper = mapper;
    }

    @RequiresPrivilege("REPUTATION.VIEW")
    @GetMapping
    public ResponseEntity<ApiResponse<ReputationResponse>> getReputation(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(getReputationUseCase.getReputation(userId))));
    }

    @RequiresPrivilege("REPUTATION.PENALTY.VIEW")
    @GetMapping("/penalties")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> listPenalties(@PathVariable String userId) {
        List<PenaltyResponse> responses = listReputationPenaltiesUseCase.list(userId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @RequiresPrivilege("USER.REPUTATION.ADJUST")
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<ReputationResponse>> adjust(@PathVariable String userId,
            @Valid @RequestBody AdjustReputationRequest request, Authentication authentication) {
        String adminId = (String) authentication.getPrincipal();
        var result = adminAdjustReputationUseCase.adjust(userId, adminId, request.delta(), request.reason());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("USER.REPUTATION.ADJUST")
    @GetMapping("/adjustments")
    public ResponseEntity<ApiResponse<List<AdjustmentResponse>>> listAdjustments(@PathVariable String userId) {
        List<AdjustmentResponse> responses = listReputationAdjustmentsUseCase.list(userId).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
