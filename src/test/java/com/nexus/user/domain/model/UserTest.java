package com.nexus.user.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void register_createsUserWithGeneratedIdAndProvidedFields() {
        User user = User.register("alice@example.com", "hashed-value", "Alice Nguyen", new RoleId("role-buyer"));

        assertThat(user.getId()).isNotBlank();
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getHashedPassword()).isEqualTo("hashed-value");
        assertThat(user.getFullName()).isEqualTo("Alice Nguyen");
        assertThat(user.getRoleId()).isEqualTo(new RoleId("role-buyer"));
        assertThat(user.getCreatedAt()).isNotNull();
    }
}
