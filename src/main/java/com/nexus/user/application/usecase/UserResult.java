package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.User;

import java.time.Instant;

public record UserResult(String id, String email, String fullName, String roleCode, Instant createdAt) {

    public static UserResult from(User user, String roleCode) {
        return new UserResult(user.getId(), user.getEmail(), user.getFullName(), roleCode, user.getCreatedAt());
    }
}
