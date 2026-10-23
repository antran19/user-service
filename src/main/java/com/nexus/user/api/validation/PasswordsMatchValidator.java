package com.nexus.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, PasswordConfirmation> {

    private String field;

    @Override
    public void initialize(PasswordsMatch annotation) {
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(PasswordConfirmation request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        String password = request.passwordValue();
        String confirmation = request.confirmationValue();
        // A missing or blank value is reported by @NotBlank on its own field; comparing would only
        // add a second, confusing "do not match" error on the same input.
        if (password == null || confirmation == null || password.isBlank() || confirmation.isBlank()) {
            return true;
        }
        if (password.equals(confirmation)) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(field)
                .addConstraintViolation();
        return false;
    }
}
