package com.nexus.user.application.usecase;

public record RegisterUserCommand(String email, String rawPassword, String fullName) {
}
