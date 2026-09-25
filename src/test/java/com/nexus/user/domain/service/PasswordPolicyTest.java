package com.nexus.user.domain.service;

import com.nexus.common.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void validate_acceptsPasswordOfEightOrMoreChars() {
        assertThatCode(() -> PasswordPolicy.validate("longenough")).doesNotThrowAnyException();
    }

    @Test
    void validate_rejectsPasswordUnderEightChars() {
        assertThatThrownBy(() -> PasswordPolicy.validate("short"))
                .isInstanceOf(ValidationException.class);
    }

    private static org.assertj.core.api.AbstractThrowableAssert<?, ?> assertThatCode(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        return org.assertj.core.api.Assertions.assertThatCode(callable);
    }
}
