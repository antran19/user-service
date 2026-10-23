package com.nexus.user.api.validation;

import com.nexus.user.api.dto.request.AdminChangePasswordRequest;
import com.nexus.user.api.dto.request.ChangePasswordRequest;
import com.nexus.user.api.dto.request.RegisterUserRequest;
import com.nexus.user.api.dto.request.ResetPasswordRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

// The password rules at the API border, checked without starting Spring: the field each error is
// reported on is what the front end uses to show it next to the right input.
class PasswordRequestsValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static <T> Map<String, String> errors(T request) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(request);
        Map<String, String> byField = new TreeMap<>();
        violations.forEach(v -> byField.put(v.getPropertyPath().toString(), v.getMessage()));
        return byField;
    }

    private static RegisterUserRequest register(String password, String confirm) {
        return new RegisterUserRequest("alice@example.com", password, confirm, "Alice Nguyen");
    }

    @Test
    void register_matchingPasswordsOfAllowedLength_isValid() {
        assertThat(errors(register("longenough", "longenough"))).isEmpty();
    }

    @Test
    void register_passwordsThatDiffer_areReportedOnConfirmPassword() {
        assertThat(errors(register("longenough", "longenougH")))
                .containsOnlyKeys("confirmPassword")
                .containsEntry("confirmPassword", "Passwords do not match");
    }

    @Test
    void register_comparisonIsExact_notCaseOrWhitespaceInsensitive() {
        assertThat(errors(register("longenough", "longenough "))).containsKey("confirmPassword");
    }

    @Test
    void register_missingConfirmation_isReportedAsMissing_notAsMismatch() {
        assertThat(errors(register("longenough", null))).containsOnlyKeys("confirmPassword");
        assertThat(errors(register("longenough", ""))).containsOnlyKeys("confirmPassword")
                .doesNotContainValue("Passwords do not match");
    }

    @Test
    void register_passwordShorterThanEight_isRejectedOnPassword() {
        assertThat(errors(register("short12", "short12")))
                .containsOnlyKeys("password")
                .containsEntry("password", "Password must be between 8 and 72 characters");
    }

    @Test
    void register_passwordLengthBounds_areEightAndSeventyTwo() {
        assertThat(errors(register("a".repeat(8), "a".repeat(8)))).isEmpty();
        assertThat(errors(register("a".repeat(72), "a".repeat(72)))).isEmpty();
        assertThat(errors(register("a".repeat(73), "a".repeat(73)))).containsOnlyKeys("password");
    }

    @Test
    void register_blankPassword_isRejected() {
        assertThat(errors(register("", ""))).containsKeys("password", "confirmPassword");
    }

    @Test
    void register_overlongEmailOrName_isRejected() {
        String longText = "x".repeat(256);
        assertThat(errors(new RegisterUserRequest(longText + "@example.com", "longenough", "longenough", "Alice")))
                .containsKey("email");
        assertThat(errors(new RegisterUserRequest("alice@example.com", "longenough", "longenough", longText)))
                .containsKey("fullName");
    }

    @Test
    void changePassword_mismatch_isReportedOnConfirmNewPassword() {
        assertThat(errors(new ChangePasswordRequest("oldpassword", "newlongpassword", "other-long-password")))
                .containsOnlyKeys("confirmNewPassword");
        assertThat(errors(new ChangePasswordRequest("oldpassword", "newlongpassword", "newlongpassword"))).isEmpty();
    }

    @Test
    void changePassword_newPasswordTooShort_isRejected() {
        assertThat(errors(new ChangePasswordRequest("oldpassword", "short", "short"))).containsOnlyKeys("newPassword");
    }

    @Test
    void resetPassword_mismatch_isReportedOnConfirmNewPassword() {
        assertThat(errors(new ResetPasswordRequest("token", "newlongpassword", "different-password")))
                .containsOnlyKeys("confirmNewPassword");
        assertThat(errors(new ResetPasswordRequest("token", "newlongpassword", "newlongpassword"))).isEmpty();
    }

    @Test
    void adminChangePassword_hasTheSameLengthRule_butNoRepeatField() {
        assertThat(errors(new AdminChangePasswordRequest("longenough"))).isEmpty();
        assertThat(errors(new AdminChangePasswordRequest("short"))).containsOnlyKeys("newPassword");
    }
}
