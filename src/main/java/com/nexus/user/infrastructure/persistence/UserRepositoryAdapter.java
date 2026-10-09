package com.nexus.user.infrastructure.persistence;

import com.nexus.user.application.port.out.UserRepositoryPort;
import com.nexus.user.domain.model.RoleId;
import com.nexus.user.domain.model.User;
import com.nexus.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                UUID.fromString(user.getId()),
                user.getEmail(),
                user.getHashedPassword(),
                user.getFullName(),
                UUID.fromString(user.getRoleId().value()),
                user.getCreatedAt(),
                user.getDeletedAt());
        userJpaRepository.save(entity);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return userJpaRepository.findById(UUID.fromString(id)).map(this::toDomain);
    }

    @Override
    public List<User> findAllNotDeleted() {
        return userJpaRepository.findByDeletedAtIsNull().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByRoleId(String roleId) {
        return userJpaRepository.existsByRoleId(UUID.fromString(roleId));
    }

    private User toDomain(UserJpaEntity entity) {
        return User.reconstitute(
                entity.getId().toString(),
                entity.getEmail(),
                entity.getHashedPassword(),
                entity.getFullName(),
                new RoleId(entity.getRoleId().toString()),
                entity.getCreatedAt(),
                entity.getDeletedAt());
    }
}
