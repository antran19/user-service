package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.RequiresPrivilege;
import com.nexus.user.api.dto.request.RateTransactionRequest;
import com.nexus.user.api.dto.response.RatingResponse;
import com.nexus.user.api.mapper.UserApiMapper;
import com.nexus.user.application.usecase.RateTransactionUseCase;
import com.nexus.user.application.usecase.RatingResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RateTransactionUseCase rateTransactionUseCase;
    private final UserApiMapper mapper;

    public RatingController(RateTransactionUseCase rateTransactionUseCase, UserApiMapper mapper) {
        this.rateTransactionUseCase = rateTransactionUseCase;
        this.mapper = mapper;
    }

    @RequiresPrivilege("REPUTATION.RATE")
    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> rate(Authentication authentication,
                                                              @Valid @RequestBody RateTransactionRequest request) {
        // raterId MUST come from the JWT -- a caller cannot submit a rating as someone else.
        String raterId = (String) authentication.getPrincipal();
        RatingResult result = rateTransactionUseCase.rate(raterId, request.ratedUserId(),
                request.transactionType(), request.transactionId(), request.score(), request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapper.toResponse(result)));
    }
}
