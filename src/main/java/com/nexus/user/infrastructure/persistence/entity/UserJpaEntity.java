package com.nexus.user.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(UUID id, String email, String hashedPassword, String fullName, UUID roleId,
                          Instant createdAt, Instant deletedAt) {
        this.id = id;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.fullName = fullName;
        this.roleId = roleId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public String getFullName() { return fullName; }
    public UUID getRoleId() { return roleId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDeletedAt() { return deletedAt; }
}
