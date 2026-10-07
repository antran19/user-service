package com.nexus.user.application.usecase;

import com.nexus.common.core.exception.ConflictException;
import com.nexus.common.core.exception.NotFoundException;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.Role;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.SellerRequest;
import com.nexus.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RequestSellerUpgradeUseCaseTest {

    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private SellerRequestRepositoryPort sellerRequestRepositoryPort;
    private RequestSellerUpgradeUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        sellerRequestRepositoryPort = mock(SellerRequestRepositoryPort.class);
        useCase = new RequestSellerUpgradeUseCase(userRepositoryPort, roleRepositoryPort, sellerRequestRepositoryPort);
    }

    @Test
    void requestUpgrade_createsPendingRequestForEligibleBuyer() {
        User buyer = User.reconstitute("user-1", "a@b.com", "hash", "A B",
                new RoleId("role-buyer"), Instant.now());
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(buyer));
        when(roleRepositoryPort.findById("role-buyer"))
                .thenReturn(Optional.of(new Role("role-buyer", "BUYER", "Buyer", Set.of())));
        when(sellerRequestRepositoryPort.findPendingByUserId("user-1")).thenReturn(Optional.empty());
        when(sellerRequestRepositoryPort.save(any(SellerRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        SellerRequestResult result = useCase.requestUpgrade("user-1");

        assertThat(result.userId()).isEqualTo("user-1");
        assertThat(result.status()).isEqualTo("PENDING");
        verify(sellerRequestRepositoryPort).save(any(SellerRequest.class));
    }

    @Test
    void requestUpgrade_rejectsWhenUserDoesNotExist() {
        when(userRepositoryPort.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.requestUpgrade("ghost")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void requestUpgrade_rejectsWhenUserIsAlreadySeller() {
        User seller = User.reconstitute("user-1", "a@b.com", "hash", "A B",
                new RoleId("role-seller"), Instant.now());
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(seller));
        when(roleRepositoryPort.findById("role-seller"))
                .thenReturn(Optional.of(new Role("role-seller", "SELLER", "Seller", Set.of())));

        assertThatThrownBy(() -> useCase.requestUpgrade("user-1")).isInstanceOf(ConflictException.class);

        verify(sellerRequestRepositoryPort, never()).save(any());
    }

    @Test
    void requestUpgrade_rejectsWhenAnotherRequestIsAlreadyPending() {
        User buyer = User.reconstitute("user-1", "a@b.com", "hash", "A B",
                new RoleId("role-buyer"), Instant.now());
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(buyer));
        when(roleRepositoryPort.findById("role-buyer"))
                .thenReturn(Optional.of(new Role("role-buyer", "BUYER", "Buyer", Set.of())));
        when(sellerRequestRepositoryPort.findPendingByUserId("user-1"))
                .thenReturn(Optional.of(SellerRequest.create("user-1")));

        assertThatThrownBy(() -> useCase.requestUpgrade("user-1")).isInstanceOf(ConflictException.class);

        verify(sellerRequestRepositoryPort, never()).save(any());
    }
}
