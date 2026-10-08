package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.RequiresPrivilege;
import com.nexus.user.api.dto.response.PenaltyResponse;
import com.nexus.user.api.dto.response.ReputationResponse;
import com.nexus.user.api.mapper.UserApiMapper;
import com.nexus.user.application.usecase.GetReputationUseCase;
import com.nexus.user.application.usecase.ListReputationPenaltiesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/reputation")
public class ReputationController {

    private final GetReputationUseCase getReputationUseCase;
    private final ListReputationPenaltiesUseCase listReputationPenaltiesUseCase;
    private final UserApiMapper mapper;

    public ReputationController(GetReputationUseCase getReputationUseCase,
                                 ListReputationPenaltiesUseCase listReputationPenaltiesUseCase,
                                 UserApiMapper mapper) {
        this.getReputationUseCase = getReputationUseCase;
        this.listReputationPenaltiesUseCase = listReputationPenaltiesUseCase;
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
}
