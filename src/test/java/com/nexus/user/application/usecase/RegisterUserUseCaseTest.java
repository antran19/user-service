package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ValidationException;
import com.nexus.common.events.UserRegisteredEvent;
import com.nexus.user.application.exception.DuplicateEmailException;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private PasswordHasherPort passwordHasherPort;
    private EventPublisherPort eventPublisherPort;
    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        passwordHasherPort = mock(PasswordHasherPort.class);
        eventPublisherPort = mock(EventPublisherPort.class);
        useCase = new RegisterUserUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort, eventPublisherPort);

        when(roleRepositoryPort.findByCode("BUYER"))
                .thenReturn(Optional.of(new Role("role-buyer", "BUYER", "Buyer", Set.of("AUTH.LOGIN"))));
        when(passwordHasherPort.hash("longenough")).thenReturn("hashed-longenough");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void register_savesNewUserWithDefaultBuyerRole() {
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        UserRegistrationResult result = useCase.register(
                new RegisterUserCommand("alice@example.com", "longenough", "Alice Nguyen", null, null, true, true));

        assertThat(result.email()).isEqualTo("alice@example.com");
        assertThat(result.fullName()).isEqualTo("Alice Nguyen");
        verify(userRepositoryPort).save(argThat(u ->
                u.getEmail().equals("alice@example.com")
                        && u.getHashedPassword().equals("hashed-longenough")
                        && u.getRoleId().value().equals("role-buyer")));
        verify(eventPublisherPort).publish(argThat(event ->
                event instanceof UserRegisteredEvent registered
                        && registered.getEmail().equals("alice@example.com")));
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepositoryPort.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> useCase.register(
                new RegisterUserCommand("alice@example.com", "longenough", "Alice Nguyen", null, null, true, true)))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void register_storesThePhoneBirthDateAndTheMomentOfConsent() {
        when(userRepositoryPort.findByEmail("alice@example.com")).thenReturn(Optional.empty());

        useCase.register(new RegisterUserCommand("alice@example.com", "longenough", "Alice Nguyen",
                "0901 234 567", LocalDate.of(1995, 6, 15), true, true));

        verify(userRepositoryPort).save(argThat(u ->
                "0901234567".equals(u.getPhone())
                        && LocalDate.of(1995, 6, 15).equals(u.getBirthDate())
                        && u.getTermsAcceptedAt() != null
                        && u.getEscrowAcknowledgedAt() != null));
    }

    @Test
    void register_withoutTheTermsConsent_isRejectedAndNothingIsSaved() {
        assertThatThrownBy(() -> useCase.register(new RegisterUserCommand("alice@example.com", "longenough",
                "Alice Nguyen", null, null, false, true)))
                .isInstanceOf(ValidationException.class);

        verify(userRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    void register_withoutTheEscrowConsent_isRejected() {
        assertThatThrownBy(() -> useCase.register(new RegisterUserCommand("alice@example.com", "longenough",
                "Alice Nguyen", null, null, true, false)))
                .isInstanceOf(ValidationException.class);

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void register_aMinor_isRejectedAndNothingIsSaved() {
        assertThatThrownBy(() -> useCase.register(new RegisterUserCommand("alice@example.com", "longenough",
                "Alice Nguyen", null, LocalDate.now().minusYears(16), true, true)))
                .isInstanceOf(ValidationException.class);

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void register_anInvalidPhone_isRejected() {
        assertThatThrownBy(() -> useCase.register(new RegisterUserCommand("alice@example.com", "longenough",
                "Alice Nguyen", "123", null, true, true)))
                .isInstanceOf(ValidationException.class);

        verify(userRepositoryPort, never()).save(any());
    }
}
