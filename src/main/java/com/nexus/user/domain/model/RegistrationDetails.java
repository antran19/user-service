package com.nexus.user.domain.model;

import java.time.Instant;
import java.time.LocalDate;

// What a person gives at sign-up beyond e-mail, password and name. Every part may be missing:
// accounts made before these fields existed, or created by an admin, have none of them.
public record RegistrationDetails(String phone, LocalDate birthDate, Instant termsAcceptedAt, Instant escrowAcknowledgedAt) {

    public static RegistrationDetails none() {
        return new RegistrationDetails(null, null, null, null);
    }
}
