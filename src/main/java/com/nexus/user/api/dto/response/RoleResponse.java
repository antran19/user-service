package com.nexus.user.api.dto.response;

import java.util.Set;

public record RoleResponse(String id, String code, String name, Set<String> privilegeCodes) {
}
