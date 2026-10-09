package com.nexus.user.infrastructure.config;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.port.out.BlacklistedTokenRepositoryPort;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.PasswordResetTokenRepositoryPort;
import com.nexus.user.application.port.out.RatingRepositoryPort;
import com.nexus.user.application.port.out.ReputationPenaltyRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.application.usecase.AdminChangeUserPasswordUseCase;
import com.nexus.user.application.usecase.ApplyAuctionPaymentTimeoutPenaltyUseCase;
import com.nexus.user.application.usecase.ChangePasswordUseCase;
import com.nexus.user.application.usecase.CreateRoleUseCase;
import com.nexus.user.application.usecase.CreateUserUseCase;
import com.nexus.user.application.usecase.DeleteRoleUseCase;
import com.nexus.user.application.usecase.DeleteUserUseCase;
import com.nexus.user.application.usecase.GetReputationUseCase;
import com.nexus.user.application.usecase.ForgotPasswordUseCase;
import com.nexus.user.application.usecase.GetRoleUseCase;
import com.nexus.user.application.usecase.GetUserUseCase;
import com.nexus.user.application.usecase.ListReputationPenaltiesUseCase;
import com.nexus.user.application.usecase.ListRolesUseCase;
import com.nexus.user.application.usecase.ListSellerRequestsUseCase;
import com.nexus.user.application.usecase.ListUsersUseCase;
import com.nexus.user.application.usecase.LoginUseCase;
import com.nexus.user.application.usecase.LogoutUseCase;
import com.nexus.user.application.usecase.RateTransactionUseCase;
import com.nexus.user.application.usecase.RegisterUserUseCase;
import com.nexus.user.application.usecase.RequestSellerUpgradeUseCase;
import com.nexus.user.application.usecase.ResetPasswordUseCase;
import com.nexus.user.application.usecase.ReviewSellerRequestUseCase;
import com.nexus.user.application.usecase.UpdateRoleUseCase;
import com.nexus.user.application.usecase.UpdateUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepositoryPort,
                                                     RoleRepositoryPort roleRepositoryPort,
                                                     PasswordHasherPort passwordHasherPort,
                                                     EventPublisherPort eventPublisherPort) {
        return new RegisterUserUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort, eventPublisherPort);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort userRepositoryPort,
                                      RoleRepositoryPort roleRepositoryPort,
                                      PasswordHasherPort passwordHasherPort,
                                      ReputationProfileRepositoryPort reputationProfileRepositoryPort,
                                      JwtTokenProvider jwtTokenProvider) {
        return new LoginUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort,
                reputationProfileRepositoryPort, jwtTokenProvider);
    }

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(UserRepositoryPort userRepositoryPort,
                                                         PasswordHasherPort passwordHasherPort) {
        return new ChangePasswordUseCase(userRepositoryPort, passwordHasherPort);
    }

    @Bean
    public RequestSellerUpgradeUseCase requestSellerUpgradeUseCase(UserRepositoryPort userRepositoryPort,
                                                                     RoleRepositoryPort roleRepositoryPort,
                                                                     SellerRequestRepositoryPort sellerRequestRepositoryPort) {
        return new RequestSellerUpgradeUseCase(userRepositoryPort, roleRepositoryPort, sellerRequestRepositoryPort);
    }

    @Bean
    public ListSellerRequestsUseCase listSellerRequestsUseCase(SellerRequestRepositoryPort sellerRequestRepositoryPort) {
        return new ListSellerRequestsUseCase(sellerRequestRepositoryPort);
    }

    @Bean
    public ReviewSellerRequestUseCase reviewSellerRequestUseCase(SellerRequestRepositoryPort sellerRequestRepositoryPort,
                                                                   UserRepositoryPort userRepositoryPort,
                                                                   RoleRepositoryPort roleRepositoryPort) {
        return new ReviewSellerRequestUseCase(sellerRequestRepositoryPort, userRepositoryPort, roleRepositoryPort);
    }

    @Bean
    public RateTransactionUseCase rateTransactionUseCase(RatingRepositoryPort ratingRepositoryPort,
                                                           ReputationProfileRepositoryPort reputationProfileRepositoryPort,
                                                           UserRepositoryPort userRepositoryPort,
                                                           EventPublisherPort eventPublisherPort) {
        return new RateTransactionUseCase(ratingRepositoryPort, reputationProfileRepositoryPort,
                userRepositoryPort, eventPublisherPort);
    }

    @Bean
    public GetReputationUseCase getReputationUseCase(ReputationProfileRepositoryPort reputationProfileRepositoryPort) {
        return new GetReputationUseCase(reputationProfileRepositoryPort);
    }

    @Bean
    public ListReputationPenaltiesUseCase listReputationPenaltiesUseCase(
            ReputationPenaltyRepositoryPort reputationPenaltyRepositoryPort) {
        return new ListReputationPenaltiesUseCase(reputationPenaltyRepositoryPort);
    }

    @Bean
    public ApplyAuctionPaymentTimeoutPenaltyUseCase applyAuctionPaymentTimeoutPenaltyUseCase(
            ReputationProfileRepositoryPort reputationProfileRepositoryPort,
            ReputationPenaltyRepositoryPort reputationPenaltyRepositoryPort,
            EventPublisherPort eventPublisherPort) {
        return new ApplyAuctionPaymentTimeoutPenaltyUseCase(reputationProfileRepositoryPort,
                reputationPenaltyRepositoryPort, eventPublisherPort);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepositoryPort userRepositoryPort,
                                                RoleRepositoryPort roleRepositoryPort,
                                                PasswordHasherPort passwordHasherPort,
                                                EventPublisherPort eventPublisherPort) {
        return new CreateUserUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort, eventPublisherPort);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepositoryPort userRepositoryPort,
                                                RoleRepositoryPort roleRepositoryPort) {
        return new UpdateUserUseCase(userRepositoryPort, roleRepositoryPort);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepositoryPort userRepositoryPort) {
        return new DeleteUserUseCase(userRepositoryPort);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort) {
        return new GetUserUseCase(userRepositoryPort, roleRepositoryPort);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepositoryPort userRepositoryPort, RoleRepositoryPort roleRepositoryPort) {
        return new ListUsersUseCase(userRepositoryPort, roleRepositoryPort);
    }

    @Bean
    public AdminChangeUserPasswordUseCase adminChangeUserPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                                                          PasswordHasherPort passwordHasherPort) {
        return new AdminChangeUserPasswordUseCase(userRepositoryPort, passwordHasherPort);
    }

    @Bean
    public CreateRoleUseCase createRoleUseCase(RoleRepositoryPort roleRepositoryPort) {
        return new CreateRoleUseCase(roleRepositoryPort);
    }

    @Bean
    public UpdateRoleUseCase updateRoleUseCase(RoleRepositoryPort roleRepositoryPort) {
        return new UpdateRoleUseCase(roleRepositoryPort);
    }

    @Bean
    public DeleteRoleUseCase deleteRoleUseCase(RoleRepositoryPort roleRepositoryPort, UserRepositoryPort userRepositoryPort) {
        return new DeleteRoleUseCase(roleRepositoryPort, userRepositoryPort);
    }

    @Bean
    public ListRolesUseCase listRolesUseCase(RoleRepositoryPort roleRepositoryPort) {
        return new ListRolesUseCase(roleRepositoryPort);
    }

    @Bean
    public GetRoleUseCase getRoleUseCase(RoleRepositoryPort roleRepositoryPort) {
        return new GetRoleUseCase(roleRepositoryPort);
    }

    @Bean
    public LogoutUseCase logoutUseCase(JwtTokenProvider jwtTokenProvider,
                                        BlacklistedTokenRepositoryPort blacklistedTokenRepositoryPort) {
        return new LogoutUseCase(jwtTokenProvider, blacklistedTokenRepositoryPort);
    }

    @Bean
    public ForgotPasswordUseCase forgotPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                                         PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
                                                         EventPublisherPort eventPublisherPort) {
        return new ForgotPasswordUseCase(userRepositoryPort, passwordResetTokenRepositoryPort, eventPublisherPort);
    }

    @Bean
    public ResetPasswordUseCase resetPasswordUseCase(UserRepositoryPort userRepositoryPort,
                                                       PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
                                                       PasswordHasherPort passwordHasherPort) {
        return new ResetPasswordUseCase(userRepositoryPort, passwordResetTokenRepositoryPort, passwordHasherPort);
    }
}
