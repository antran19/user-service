package com.nexus.user.api.dto.response;

import java.time.Instant;

public record UserAdminResponse(String id, String email, String fullName, String roleCode, Instant createdAt) {
}
