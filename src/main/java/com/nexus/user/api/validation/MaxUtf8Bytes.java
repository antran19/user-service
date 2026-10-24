package com.nexus.user.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// BCrypt only uses the first 72 BYTES of a password, not 72 characters. In UTF-8 an unaccented
// letter is 1 byte but a Vietnamese letter with a tone mark is 3, so a 30-character password with
// accents can already be over the limit. @Size counts characters; this counts bytes.
@Documented
@Constraint(validatedBy = MaxUtf8BytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxUtf8Bytes {

    int value() default 72;

    String message() default "Password must be at most {value} bytes (a letter with an accent takes 2 to 3 bytes)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
