package com.nexus.user.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class User {
    private final String id;
    private final String email;
    private final String hashedPassword;
    private final String fullName;
    private final RoleId roleId;
    private final Instant createdAt;
    private final Instant deletedAt;
    private final RegistrationDetails details;

    private User(String id, String email, String hashedPassword, String fullName, RoleId roleId,
                  Instant createdAt, Instant deletedAt, RegistrationDetails details) {
        this.id = id;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.fullName = fullName;
        this.roleId = roleId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
        this.details = details == null ? RegistrationDetails.none() : details;
    }

    public static User register(String email, String hashedPassword, String fullName, RoleId roleId) {
        return register(email, hashedPassword, fullName, roleId, RegistrationDetails.none());
    }

    public static User register(String email, String hashedPassword, String fullName, RoleId roleId,
                                 RegistrationDetails details) {
        return new User(UUID.randomUUID().toString(), email, hashedPassword, fullName, roleId, Instant.now(), null, details);
    }

    public static User reconstitute(String id, String email, String hashedPassword, String fullName, RoleId roleId,
                                     Instant createdAt, Instant deletedAt) {
        return new User(id, email, hashedPassword, fullName, roleId, createdAt, deletedAt, RegistrationDetails.none());
    }

    public static User reconstitute(String id, String email, String hashedPassword, String fullName, RoleId roleId,
                                     Instant createdAt, Instant deletedAt, RegistrationDetails details) {
        return new User(id, email, hashedPassword, fullName, roleId, createdAt, deletedAt, details);
    }

    public User withHashedPassword(String newHashedPassword) {
        return new User(id, email, newHashedPassword, fullName, roleId, createdAt, deletedAt, details);
    }

    public User withRoleId(RoleId newRoleId) {
        return new User(id, email, hashedPassword, fullName, newRoleId, createdAt, deletedAt, details);
    }

    public User withPhone(String newPhone) {
        return new User(id, email, hashedPassword, fullName, roleId, createdAt, deletedAt,
                new RegistrationDetails(newPhone, details.birthDate(), details.termsAcceptedAt(), details.escrowAcknowledgedAt()));
    }

    public User withFullName(String newFullName) {
        return new User(id, email, hashedPassword, newFullName, roleId, createdAt, deletedAt, details);
    }

    // Soft delete (SRS config USER_SOFT_DELETE=true): keeps the row so FK references from
    // ratings/orders/etc. stay intact, but the account can no longer log in or be found by
    // normal lookups once deleted.
    public User delete() {
        return new User(id, email, hashedPassword, fullName, roleId, createdAt, Instant.now(), details);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFullName() { return fullName; }
    public RoleId getRoleId() { return roleId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public RegistrationDetails getDetails() { return details; }
    public String getPhone() { return details.phone(); }
    public LocalDate getBirthDate() { return details.birthDate(); }
    public Instant getTermsAcceptedAt() { return details.termsAcceptedAt(); }
    public Instant getEscrowAcknowledgedAt() { return details.escrowAcknowledgedAt(); }
}
