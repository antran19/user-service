package com.nexus.user.domain.service;

import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.domain.model.RegistrationDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationDetailsPolicyTest {

    private static final Instant NOW = Instant.parse("2026-11-05T03:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 5);

    private static RegistrationDetails validate(String phone, LocalDate birthDate, boolean terms, boolean escrow) {
        return RegistrationDetailsPolicy.validate(phone, birthDate, terms, escrow, NOW, TODAY);
    }

    private static java.util.List<String> failedFields(String phone, LocalDate birthDate, boolean terms, boolean escrow) {
        try {
            validate(phone, birthDate, terms, escrow);
            return java.util.List.of();
        } catch (ValidationException e) {
            return e.getFieldErrors().stream().map(f -> f.field()).toList();
        }
    }

    @Test
    void justTheTwoConsents_isEnough_andTheOptionalPartsStayEmpty() {
        RegistrationDetails details = validate(null, null, true, true);

        assertThat(details.phone()).isNull();
        assertThat(details.birthDate()).isNull();
        assertThat(details.termsAcceptedAt()).isEqualTo(NOW);
        assertThat(details.escrowAcknowledgedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0901234567", "+84901234567", "0901 234 567", "0901.234.567", "0901-234-567", "  0901234567  "})
    void aVietnamesePhone_isAccepted_andStoredWithoutSeparators(String phone) {
        String stored = validate(phone, null, true, true).phone();

        assertThat(stored).matches("^(0\\d{9}|\\+84\\d{9})$");
    }

    @Test
    void aPhoneWrittenWithSeparators_isNormalised() {
        assertThat(validate("0901 234-567", null, true, true).phone()).isEqualTo("0901234567");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "090123456", "09012345678", "abcdefghij", "+8490123456", "84901234567", "0901234567x"})
    void anInvalidPhone_isRejected(String phone) {
        assertThat(failedFields(phone, null, true, true)).containsExactly("phone");
    }

    @Test
    void aBlankPhone_isTreatedAsNotGiven() {
        assertThat(validate("   ", null, true, true).phone()).isNull();
    }

    @Test
    void someoneExactlyEighteenToday_isAccepted() {
        assertThat(validate(null, TODAY.minusYears(18), true, true).birthDate()).isEqualTo(TODAY.minusYears(18));
    }

    @Test
    void someoneTurningEighteenTomorrow_isRejected() {
        assertThat(failedFields(null, TODAY.minusYears(18).plusDays(1), true, true)).containsExactly("birthDate");
    }

    @Test
    void aMinor_isRejected() {
        assertThatThrownBy(() -> validate(null, TODAY.minusYears(15), true, true))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getFieldErrors().get(0).message()).contains("18"));
    }

    @Test
    void aBirthDateInTheFuture_isRejected() {
        assertThat(failedFields(null, TODAY.plusDays(1), true, true)).containsExactly("birthDate");
    }

    @Test
    void aBirthDateBefore1900_isRejected() {
        assertThat(failedFields(null, LocalDate.of(1899, 12, 31), true, true)).containsExactly("birthDate");
    }

    @Test
    void notAcceptingTheTerms_isRejected() {
        assertThat(failedFields(null, null, false, true)).containsExactly("termsAccepted");
    }

    @Test
    void notAcknowledgingTheEscrowRules_isRejected() {
        assertThat(failedFields(null, null, true, false)).containsExactly("escrowAcknowledged");
    }

    @Test
    void everyProblemIsReportedAtOnce() {
        assertThat(failedFields("abc", TODAY.minusYears(10), false, false))
                .containsExactlyInAnyOrder("phone", "birthDate", "termsAccepted", "escrowAcknowledged");
    }
}
