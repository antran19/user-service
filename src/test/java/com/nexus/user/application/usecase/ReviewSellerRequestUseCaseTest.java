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

class ReviewSellerRequestUseCaseTest {

    private SellerRequestRepositoryPort sellerRequestRepositoryPort;
    private UserRepositoryPort userRepositoryPort;
    private RoleRepositoryPort roleRepositoryPort;
    private ReviewSellerRequestUseCase useCase;
    private SellerRequest pendingRequest;
    private User requestingUser;

    @BeforeEach
    void setUp() {
        sellerRequestRepositoryPort = mock(SellerRequestRepositoryPort.class);
        userRepositoryPort = mock(UserRepositoryPort.class);
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        useCase = new ReviewSellerRequestUseCase(sellerRequestRepositoryPort, userRepositoryPort, roleRepositoryPort);

        pendingRequest = SellerRequest.create("user-1");
        requestingUser = User.reconstitute("user-1", "a@b.com", "hash", "A B",
                new RoleId("role-buyer"), Instant.now());

        when(sellerRequestRepositoryPort.findById("req-1")).thenReturn(Optional.of(pendingRequest));
        when(sellerRequestRepositoryPort.save(any(SellerRequest.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void approve_flipsUserRoleToSellerAndMarksRequestApproved() {
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(requestingUser));
        when(roleRepositoryPort.findByCode("SELLER"))
                .thenReturn(Optional.of(new Role("role-seller", "SELLER", "Seller", Set.of())));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        SellerRequestResult result = useCase.review("req-1", "admin-1", true);

        assertThat(result.status()).isEqualTo("APPROVED");
        verify(userRepositoryPort).save(argThat(u -> u.getRoleId().value().equals("role-seller")));
    }

    @Test
    void reject_marksRequestRejectedWithoutTouchingUserRole() {
        SellerRequestResult result = useCase.review("req-1", "admin-1", false);

        assertThat(result.status()).isEqualTo("REJECTED");
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void review_rejectsUnknownRequestId() {
        when(sellerRequestRepositoryPort.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.review("ghost", "admin-1", true)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void review_rejectsAlreadyReviewedRequest() {
        SellerRequest alreadyApproved = pendingRequest.approve("admin-0");
        when(sellerRequestRepositoryPort.findById("req-1")).thenReturn(Optional.of(alreadyApproved));

        assertThatThrownBy(() -> useCase.review("req-1", "admin-1", true)).isInstanceOf(ConflictException.class);

        verify(sellerRequestRepositoryPort, never()).save(any());
    }
}
