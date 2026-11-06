package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.NotFoundException;
import com.nexus.common.core.exception.ValidationException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.RegistrationDetails;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileUseCasesTest {

    private static final Instant CONSENT = Instant.parse("2026-11-05T03:00:00Z");

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private GetMyProfileUseCase getUseCase;
    private UpdateMyProfileUseCase updateUseCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        getUseCase = new GetMyProfileUseCase(userRepositoryPort, roleRepositoryPort);
        updateUseCase = new UpdateMyProfileUseCase(userRepositoryPort, roleRepositoryPort);
        when(roleRepositoryPort.findById("role-buyer"))
                .thenReturn(Optional.of(new Role("role-buyer", "BUYER", "Buyer", Set.of("PROFILE.VIEW"))));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User alice() {
        return User.reconstitute("user-1", "alice@example.com", "hash", "Alice Nguyen", new RoleId("role-buyer"),
                Instant.parse("2026-10-01T00:00:00Z"), null,
                new RegistrationDetails("0901234567", LocalDate.of(1995, 6, 15), CONSENT, CONSENT));
    }

    private void aliceExists() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(alice()));
    }

    // ---- get

    @Test
    void get_returnsTheAccountWithItsDetails_andNeverThePasswordHash() {
        aliceExists();

        ProfileResult profile = getUseCase.get("user-1");

        assertThat(profile.email()).isEqualTo("alice@example.com");
        assertThat(profile.fullName()).isEqualTo("Alice Nguyen");
        assertThat(profile.roleCode()).isEqualTo("BUYER");
        assertThat(profile.phone()).isEqualTo("0901234567");
        assertThat(profile.birthDate()).isEqualTo(LocalDate.of(1995, 6, 15));
        assertThat(profile.termsAcceptedAt()).isEqualTo(CONSENT);
        assertThat(profile.toString()).doesNotContain("hash");
    }

    @Test
    void get_anAccountCreatedBeforeTheDetailsExisted_hasThemEmpty() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(User.reconstitute("user-1",
                "old@example.com", "hash", "Old Account", new RoleId("role-buyer"), Instant.now(), null)));

        ProfileResult profile = getUseCase.get("user-1");

        assertThat(profile.phone()).isNull();
        assertThat(profile.birthDate()).isNull();
        assertThat(profile.termsAcceptedAt()).isNull();
    }

    @Test
    void get_aDeletedAccount_isNotFound() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(alice().delete()));

        assertThatThrownBy(() -> getUseCase.get("user-1")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void get_anUnknownAccount_isNotFound() {
        when(userRepositoryPort.findById("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUseCase.get("nobody")).isInstanceOf(NotFoundException.class);
    }

    // ---- update

    @Test
    void update_changesTheNameAndThePhone_andNothingElse() {
        aliceExists();

        ProfileResult profile = updateUseCase.update("user-1", "  Alice N. Nguyen  ", "0988 777 666");

        assertThat(profile.fullName()).isEqualTo("Alice N. Nguyen");
        assertThat(profile.phone()).isEqualTo("0988777666");
        verify(userRepositoryPort).save(argThat(u -> u.getEmail().equals("alice@example.com")
                && u.getRoleId().value().equals("role-buyer")
                && LocalDate.of(1995, 6, 15).equals(u.getBirthDate())
                && CONSENT.equals(u.getTermsAcceptedAt())
                && CONSENT.equals(u.getEscrowAcknowledgedAt())
                && u.getHashedPassword().equals("hash")));
    }

    @Test
    void update_aBlankPhone_clearsThePhone() {
        aliceExists();

        ProfileResult profile = updateUseCase.update("user-1", "Alice Nguyen", "  ");

        assertThat(profile.phone()).isNull();
    }

    @Test
    void update_aNullPhone_clearsThePhone() {
        aliceExists();

        assertThat(updateUseCase.update("user-1", "Alice Nguyen", null).phone()).isNull();
    }

    @Test
    void update_anInvalidPhone_isRejected_andNothingIsSaved() {
        aliceExists();

        assertThatThrownBy(() -> updateUseCase.update("user-1", "Alice Nguyen", "12345"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getFieldErrors()).extracting(f -> f.field()).containsExactly("phone"));

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void update_aBlankName_isRejected() {
        aliceExists();

        assertThatThrownBy(() -> updateUseCase.update("user-1", "   ", "0901234567"))
                .isInstanceOfSatisfying(ValidationException.class,
                        e -> assertThat(e.getFieldErrors()).extracting(f -> f.field()).containsExactly("fullName"));

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void update_aNameOver255Characters_isRejected() {
        aliceExists();

        assertThatThrownBy(() -> updateUseCase.update("user-1", "x".repeat(256), null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void update_aDeletedAccount_isNotFound() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(alice().delete()));

        assertThatThrownBy(() -> updateUseCase.update("user-1", "Alice", null)).isInstanceOf(NotFoundException.class);

        verify(userRepositoryPort, never()).save(any());
    }
}
