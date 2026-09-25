package com.nexus.user.domain.model;

import java.util.Set;

public record Role(String id, String code, String name, Set<String> privilegeCodes) {
}
