package com.nexus.user.domain.model;

import java.time.Instant;
import java.util.UUID;

public class User {
    private final String id;
    private final String email;
    private final String hashedPassword;
    private final String fullName;
    private final RoleId roleId;
    private final Instant createdAt;

    private User(String id, String email, String hashedPassword, String fullName, RoleId roleId, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.fullName = fullName;
        this.roleId = roleId;
        this.createdAt = createdAt;
    }

    public static User register(String email, String hashedPassword, String fullName, RoleId roleId) {
        return new User(UUID.randomUUID().toString(), email, hashedPassword, fullName, roleId, Instant.now());
    }

    public static User reconstitute(String id, String email, String hashedPassword, String fullName, RoleId roleId, Instant createdAt) {
        return new User(id, email, hashedPassword, fullName, roleId, createdAt);
    }

    public User withHashedPassword(String newHashedPassword) {
        return new User(id, email, newHashedPassword, fullName, roleId, createdAt);
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFullName() { return fullName; }
    public RoleId getRoleId() { return roleId; }
    public Instant getCreatedAt() { return createdAt; }
}
