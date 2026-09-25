package com.nexus.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "privileges")
public class PrivilegeJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    protected PrivilegeJpaEntity() {
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
}
