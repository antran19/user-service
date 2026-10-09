package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.user.api.dto.request.ForgotPasswordRequest;
import com.nexus.user.api.dto.request.LoginRequest;
import com.nexus.user.api.dto.request.ResetPasswordRequest;
import com.nexus.user.api.dto.response.AuthResponse;
import com.nexus.user.api.dto.response.ForgotPasswordResponse;
import com.nexus.user.application.usecase.ForgotPasswordUseCase;
import com.nexus.user.application.usecase.LoginCommand;
import com.nexus.user.application.usecase.LoginResult;
import com.nexus.user.application.usecase.LoginUseCase;
import com.nexus.user.application.usecase.LogoutUseCase;
import com.nexus.user.application.usecase.ResetPasswordUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public AuthController(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase,
                           ForgotPasswordUseCase forgotPasswordUseCase, ResetPasswordUseCase resetPasswordUseCase) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(ApiResponse.ok(new AuthResponse(result.token())));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        logoutUseCase.logout(authorizationHeader.substring("Bearer ".length()));
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String resetToken = forgotPasswordUseCase.requestReset(request.email());
        return ResponseEntity.ok(ApiResponse.ok(new ForgotPasswordResponse(resetToken)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
