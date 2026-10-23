package com.nexus.user.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Class-level check that the password and its repeat are equal. The error is reported on the
// REPEAT field (default "confirmPassword") so the client can show it next to that input; it is
// enforced on the server because the front end's own check is skipped by anyone calling the API.
@Documented
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordsMatch {

    String message() default "Passwords do not match";

    // Name of the field the violation is attached to.
    String field() default "confirmPassword";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
