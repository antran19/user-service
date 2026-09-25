package com.nexus.user.api.mapper;

import com.nexus.user.api.dto.request.RegisterUserRequest;
import com.nexus.user.api.dto.response.UserResponse;
import com.nexus.user.application.usecase.RegisterUserCommand;
import com.nexus.user.application.usecase.UserRegistrationResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserApiMapper {

    @Mapping(source = "password", target = "rawPassword")
    RegisterUserCommand toCommand(RegisterUserRequest request);

    @Mapping(source = "userId", target = "id")
    UserResponse toResponse(UserRegistrationResult result);
}
