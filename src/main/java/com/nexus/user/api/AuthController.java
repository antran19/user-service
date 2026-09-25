package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.user.api.dto.request.LoginRequest;
import com.nexus.user.api.dto.response.AuthResponse;
import com.nexus.user.application.usecase.LoginCommand;
import com.nexus.user.application.usecase.LoginResult;
import com.nexus.user.application.usecase.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(ApiResponse.ok(new AuthResponse(result.token())));
    }
}
