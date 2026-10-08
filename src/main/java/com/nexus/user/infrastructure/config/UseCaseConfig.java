package com.nexus.user.infrastructure.config;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RatingRepositoryPort;
import com.nexus.user.application.port.out.ReputationPenaltyRepositoryPort;
import com.nexus.user.application.port.out.ReputationProfileRepositoryPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.application.usecase.ApplyAuctionPaymentTimeoutPenaltyUseCase;
import com.nexus.user.application.usecase.ChangePasswordUseCase;
import com.nexus.user.application.usecase.GetReputationUseCase;
import com.nexus.user.application.usecase.ListReputationPenaltiesUseCase;
import com.nexus.user.application.usecase.ListSellerRequestsUseCase;
import com.nexus.user.application.usecase.LoginUseCase;
import com.nexus.user.application.usecase.RateTransactionUseCase;
import com.nexus.user.application.usecase.RegisterUserUseCase;
import com.nexus.user.application.usecase.RequestSellerUpgradeUseCase;
import com.nexus.user.application.usecase.ReviewSellerRequestUseCase;
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
}
