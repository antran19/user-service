package com.nexus.user.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class MaxUtf8BytesValidator implements ConstraintValidator<MaxUtf8Bytes, String> {

    private int max;

    @Override
    public void initialize(MaxUtf8Bytes annotation) {
        this.max = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null is @NotBlank's job
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
