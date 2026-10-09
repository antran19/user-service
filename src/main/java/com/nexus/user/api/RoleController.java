package com.nexus.user.api;

import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.RequiresPrivilege;
import com.nexus.user.api.dto.request.CreateRoleRequest;
import com.nexus.user.api.dto.request.UpdateRoleRequest;
import com.nexus.user.api.dto.response.RoleResponse;
import com.nexus.user.api.mapper.UserApiMapper;
import com.nexus.user.application.usecase.CreateRoleUseCase;
import com.nexus.user.application.usecase.DeleteRoleUseCase;
import com.nexus.user.application.usecase.GetRoleUseCase;
import com.nexus.user.application.usecase.ListRolesUseCase;
import com.nexus.user.application.usecase.RoleResult;
import com.nexus.user.application.usecase.UpdateRoleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final CreateRoleUseCase createRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final ListRolesUseCase listRolesUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final UserApiMapper mapper;

    public RoleController(CreateRoleUseCase createRoleUseCase, UpdateRoleUseCase updateRoleUseCase,
                           DeleteRoleUseCase deleteRoleUseCase, ListRolesUseCase listRolesUseCase,
                           GetRoleUseCase getRoleUseCase, UserApiMapper mapper) {
        this.createRoleUseCase = createRoleUseCase;
        this.updateRoleUseCase = updateRoleUseCase;
        this.deleteRoleUseCase = deleteRoleUseCase;
        this.listRolesUseCase = listRolesUseCase;
        this.getRoleUseCase = getRoleUseCase;
        this.mapper = mapper;
    }

    @RequiresPrivilege("ROLE.CREATE")
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody CreateRoleRequest request) {
        RoleResult result = createRoleUseCase.create(request.code(), request.name(), request.privilegeCodes());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("ROLE.LIST")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> list() {
        List<RoleResponse> results = listRolesUseCase.list().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @RequiresPrivilege("ROLE.VIEW")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(getRoleUseCase.get(id))));
    }

    @RequiresPrivilege("ROLE.UPDATE")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> update(@PathVariable String id,
                                                               @Valid @RequestBody UpdateRoleRequest request) {
        RoleResult result = updateRoleUseCase.update(id, request.name(), request.privilegeCodes());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(result)));
    }

    @RequiresPrivilege("ROLE.DELETE")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        deleteRoleUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
