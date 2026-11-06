package com.nexus.user.api.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record ProfileResponse(String id, String email, String fullName, String roleCode, String phone,
                              LocalDate birthDate, Instant termsAcceptedAt, Instant escrowAcknowledgedAt,
                              Instant createdAt) {
}
