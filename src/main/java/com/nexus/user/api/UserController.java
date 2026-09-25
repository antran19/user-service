package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.RequiresPrivilege;
import com.nexus.user.api.dto.request.ChangePasswordRequest;
import com.nexus.user.api.dto.request.RegisterUserRequest;
import com.nexus.user.api.dto.response.UserResponse;
import com.nexus.user.api.mapper.UserApiMapper;
import com.nexus.user.application.usecase.ChangePasswordCommand;
import com.nexus.user.application.usecase.ChangePasswordUseCase;
import com.nexus.user.application.usecase.RegisterUserUseCase;
import com.nexus.user.application.usecase.UserRegistrationResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserApiMapper mapper;
    private final ChangePasswordUseCase changePasswordUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase, UserApiMapper mapper,
                           ChangePasswordUseCase changePasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.mapper = mapper;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        UserRegistrationResult result = registerUserUseCase.register(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("PROFILE.CHANGE_PASSWORD")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication authentication,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        String userId = (String) authentication.getPrincipal();
        changePasswordUseCase.changePassword(
                new ChangePasswordCommand(userId, request.oldPassword(), request.newPassword()));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
