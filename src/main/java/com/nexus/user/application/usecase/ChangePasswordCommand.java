package com.nexus.user.application.usecase;

public record ChangePasswordCommand(String userId, String oldRawPassword, String newRawPassword) {
}
