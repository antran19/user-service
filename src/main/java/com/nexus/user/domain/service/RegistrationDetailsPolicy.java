package com.nexus.user.domain.service;

import com.nexus.common.core.FieldError;
import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.domain.model.RegistrationDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

// The rules of the sign-up form, enforced on the server: the form checks them too, but anyone who
// calls the API directly skips the form. All the problems found are reported together.
public final class RegistrationDetailsPolicy {

    public static final int MINIMUM_AGE = 18;
    private static final Pattern SEPARATORS = Pattern.compile("[\\s.-]");
    private static final Pattern VIETNAMESE_PHONE = Pattern.compile("^(0\\d{9}|\\+84\\d{9})$");
    private static final LocalDate EARLIEST_BIRTH_DATE = LocalDate.of(1900, 1, 1);

    private RegistrationDetailsPolicy() {
    }

    // phone and birthDate are optional, but checked when given. The two consents are required.
    public static RegistrationDetails validate(String phone, LocalDate birthDate, boolean termsAccepted,
                                               boolean escrowAcknowledged, Instant now, LocalDate today) {
        List<FieldError> errors = new ArrayList<>();

        String normalizedPhone = null;
        if (phone != null && !phone.isBlank()) {
            normalizedPhone = SEPARATORS.matcher(phone.trim()).replaceAll("");
            if (!VIETNAMESE_PHONE.matcher(normalizedPhone).matches()) {
                errors.add(new FieldError("phone", "Phone must be a Vietnamese number (0xxxxxxxxx or +84xxxxxxxxx)"));
                normalizedPhone = null;
            }
        }

        if (birthDate != null) {
            if (birthDate.isBefore(EARLIEST_BIRTH_DATE) || birthDate.isAfter(today)) {
                errors.add(new FieldError("birthDate", "Date of birth is not valid"));
            } else if (Period.between(birthDate, today).getYears() < MINIMUM_AGE) {
                errors.add(new FieldError("birthDate", "You must be at least " + MINIMUM_AGE + " years old to join"));
            }
        }

        if (!termsAccepted) {
            errors.add(new FieldError("termsAccepted", "The terms of service must be accepted"));
        }
        if (!escrowAcknowledged) {
            errors.add(new FieldError("escrowAcknowledged", "The escrow rules must be acknowledged"));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        return new RegistrationDetails(normalizedPhone, birthDate, now, now);
    }
}
