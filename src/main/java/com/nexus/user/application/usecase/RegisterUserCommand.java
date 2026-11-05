package com.nexus.user.application.usecase;

import java.time.LocalDate;

public record RegisterUserCommand(String email, String rawPassword, String fullName, String phone,
                                  LocalDate birthDate, boolean termsAccepted, boolean escrowAcknowledged) {
}
