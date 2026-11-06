package com.nexus.user.application.usecase;

import com.nexus.user.domain.model.User;

import java.time.Instant;
import java.time.LocalDate;

// What a person sees of their own account. Never the password hash.
public record ProfileResult(String id, String email, String fullName, String roleCode, String phone,
                            LocalDate birthDate, Instant termsAcceptedAt, Instant escrowAcknowledgedAt,
                            Instant createdAt) {

    public static ProfileResult from(User user, String roleCode) {
        return new ProfileResult(user.getId(), user.getEmail(), user.getFullName(), roleCode, user.getPhone(),
                user.getBirthDate(), user.getTermsAcceptedAt(), user.getEscrowAcknowledgedAt(), user.getCreatedAt());
    }
}
