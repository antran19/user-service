package com.nexus.user.application.usecase;

public record LoginCommand(String email, String rawPassword) {
}
