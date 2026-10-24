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

    // ---- 72 BYTES, not 72 characters (BCrypt)

    // "ạ" is 3 bytes in UTF-8: 24 of them are exactly 72 bytes, 25 are 75.
    @Test
    void password_withAccents_isLimitedByBytesNotByCharacters() {
        String atTheLimit = "ạ".repeat(24);
        String overTheLimit = "ạ".repeat(25);

        assertThat(errors(register(atTheLimit, atTheLimit))).isEmpty();
        assertThat(errors(register(overTheLimit, overTheLimit))).containsOnlyKeys("password");
        assertThat(errors(register(overTheLimit, overTheLimit)).get("password")).contains("72 bytes");
    }

    @Test
    void password_ofManyAccentedWordsThatIsShortInCharacters_isStillRejectedWhenOver72Bytes() {
        String thirtyChars = "Mật khẩu của tôi rất là dài nè"; // 30 characters, 40+ bytes: fine
        String fiftyChars = "Mật khẩu tiếng Việt có dấu rất là dài để thử giới hạn"; // 50 characters, over 72 bytes

        assertThat(errors(register(thirtyChars, thirtyChars))).isEmpty();
        assertThat(fiftyChars.length()).isLessThan(73);
        assertThat(fiftyChars.getBytes(java.nio.charset.StandardCharsets.UTF_8).length).isGreaterThan(72);
        assertThat(errors(register(fiftyChars, fiftyChars))).containsOnlyKeys("password");
    }

    @Test
    void theByteRule_appliesToChangeResetAndAdminSettingToo() {
        String tooManyBytes = "ạ".repeat(25);

        assertThat(errors(new ChangePasswordRequest("oldpassword", tooManyBytes, tooManyBytes))).containsKey("newPassword");
        assertThat(errors(new ResetPasswordRequest("token", tooManyBytes, tooManyBytes))).containsKey("newPassword");
        assertThat(errors(new AdminChangePasswordRequest(tooManyBytes))).containsOnlyKeys("newPassword");
    }

    @Test
    void adminCreateUser_hasThePasswordRuleToo() {
        assertThat(errors(new com.nexus.user.api.dto.request.CreateUserRequest("a@b.com", "short", "Name", "BUYER")))
                .containsOnlyKeys("password");
        assertThat(errors(new com.nexus.user.api.dto.request.CreateUserRequest("a@b.com", "longenough", "Name", "BUYER"))).isEmpty();
        assertThat(errors(new com.nexus.user.api.dto.request.CreateUserRequest("a@b.com", "ạ".repeat(25), "Name", "BUYER")))
                .containsOnlyKeys("password");
    }

    // Records what the encoder itself does past 72 bytes, so the reason for the rule is on file.
    @Test
    void theEncoderItself_behaviourBeyond72Bytes() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String longPassword = "ạ".repeat(30); // 90 bytes
        try {
            String hash = encoder.encode(longPassword);
            // Not rejected: only the first 72 bytes count, so a different tail still matches.
            assertThat(encoder.matches("ạ".repeat(24) + "something else", hash)).isTrue();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).containsIgnoringCase("72");
        }
    }
}
