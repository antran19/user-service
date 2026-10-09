package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.RequiresPrivilege;
import com.nexus.user.api.dto.request.AdminChangePasswordRequest;
import com.nexus.user.api.dto.request.ChangePasswordRequest;
import com.nexus.user.api.dto.request.CreateUserRequest;
import com.nexus.user.api.dto.request.RegisterUserRequest;
import com.nexus.user.api.dto.request.UpdateUserRequest;
import com.nexus.user.api.dto.response.UserAdminResponse;
import com.nexus.user.api.dto.response.UserResponse;
import com.nexus.user.api.mapper.UserApiMapper;
import com.nexus.user.application.usecase.AdminChangeUserPasswordUseCase;
import com.nexus.user.application.usecase.ChangePasswordCommand;
import com.nexus.user.application.usecase.ChangePasswordUseCase;
import com.nexus.user.application.usecase.CreateUserUseCase;
import com.nexus.user.application.usecase.DeleteUserUseCase;
import com.nexus.user.application.usecase.GetUserUseCase;
import com.nexus.user.application.usecase.ListUsersUseCase;
import com.nexus.user.application.usecase.RegisterUserUseCase;
import com.nexus.user.application.usecase.UpdateUserUseCase;
import com.nexus.user.application.usecase.UserRegistrationResult;
import com.nexus.user.application.usecase.UserResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserApiMapper mapper;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final ListUsersUseCase listUsersUseCase;
    private final AdminChangeUserPasswordUseCase adminChangeUserPasswordUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase, UserApiMapper mapper,
                           ChangePasswordUseCase changePasswordUseCase, CreateUserUseCase createUserUseCase,
                           UpdateUserUseCase updateUserUseCase,
                           DeleteUserUseCase deleteUserUseCase, GetUserUseCase getUserUseCase,
                           ListUsersUseCase listUsersUseCase,
                           AdminChangeUserPasswordUseCase adminChangeUserPasswordUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.mapper = mapper;
        this.changePasswordUseCase = changePasswordUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.listUsersUseCase = listUsersUseCase;
        this.adminChangeUserPasswordUseCase = adminChangeUserPasswordUseCase;
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

    @RequiresPrivilege("USER.CREATE")
    @PostMapping
    public ResponseEntity<ApiResponse<UserAdminResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        UserResult result = createUserUseCase.create(request.email(), request.password(), request.fullName(),
                request.roleCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("USER.LIST")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAdminResponse>>> list() {
        List<UserAdminResponse> results = listUsersUseCase.list().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @RequiresPrivilege("USER.VIEW")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(getUserUseCase.get(id))));
    }

    @RequiresPrivilege("USER.UPDATE")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserAdminResponse>> update(@PathVariable String id,
                                                                   @Valid @RequestBody UpdateUserRequest request) {
        UserResult result = updateUserUseCase.update(id, request.fullName(), request.roleCode());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("USER.DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        deleteUserUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @RequiresPrivilege("USER.CHANGE_PASSWORD")
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> adminChangePassword(@PathVariable String id,
                                                                    @Valid @RequestBody AdminChangePasswordRequest request) {
        adminChangeUserPasswordUseCase.changePassword(id, request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
