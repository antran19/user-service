package com.nexus.user.infrastructure.config;

import com.nexus.common.security.JwtTokenProvider;
import com.nexus.user.application.port.out.EventPublisherPort;
import com.nexus.user.application.port.out.PasswordHasherPort;
import com.nexus.user.application.port.out.RoleRepositoryPort;
import com.nexus.user.application.port.out.SellerRequestRepositoryPort;
import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.application.usecase.ChangePasswordUseCase;
import com.nexus.user.application.usecase.ListSellerRequestsUseCase;
import com.nexus.user.application.usecase.LoginUseCase;
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
                                      JwtTokenProvider jwtTokenProvider) {
        return new LoginUseCase(userRepositoryPort, roleRepositoryPort, passwordHasherPort, jwtTokenProvider);
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
}
