package com.nexus.user.api.mapper;

import com.nexus.user.api.dto.request.RegisterUserRequest;
import com.nexus.user.api.dto.response.PenaltyResponse;
import com.nexus.user.api.dto.response.RatingResponse;
import com.nexus.user.api.dto.response.ReputationResponse;
import com.nexus.user.api.dto.response.SellerRequestResponse;
import com.nexus.user.api.dto.response.UserResponse;
import com.nexus.user.application.usecase.PenaltyResult;
import com.nexus.user.application.usecase.RatingResult;
import com.nexus.user.application.usecase.RegisterUserCommand;
import com.nexus.user.application.usecase.ReputationResult;
import com.nexus.user.application.usecase.SellerRequestResult;
import com.nexus.user.application.usecase.UserRegistrationResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserApiMapper {

    @Mapping(source = "password", target = "rawPassword")
    RegisterUserCommand toCommand(RegisterUserRequest request);

    @Mapping(source = "userId", target = "id")
    UserResponse toResponse(UserRegistrationResult result);

    SellerRequestResponse toResponse(SellerRequestResult result);

    RatingResponse toResponse(RatingResult result);

    ReputationResponse toResponse(ReputationResult result);

    PenaltyResponse toResponse(PenaltyResult result);
}
