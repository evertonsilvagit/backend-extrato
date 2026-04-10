package br.com.everton.backendextrato.infrastructure.auth;

import br.com.everton.backendextrato.application.auth.port.out.UserProfileRepository;
import br.com.everton.backendextrato.domain.auth.UserProfile;
import br.com.everton.backendextrato.model.UserAccount;
import br.com.everton.backendextrato.repository.UserAccountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserProfileJpaAdapter implements UserProfileRepository {

    private final UserAccountRepository userAccountRepository;

    public UserProfileJpaAdapter(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Optional<UserProfile> findByEmail(String email) {
        return userAccountRepository.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        UserAccount entity = userProfile.id() != null
                ? userAccountRepository.findById(userProfile.id()).orElseGet(UserAccount::new)
                : new UserAccount();

        entity.setEmail(userProfile.email());
        entity.setDisplayName(userProfile.displayName());
        entity.setProfileImageUrl(userProfile.profileImageUrl());
        entity.setPasswordHash(userProfile.passwordHash());

        return toDomain(userAccountRepository.save(entity));
    }

    private UserProfile toDomain(UserAccount entity) {
        return new UserProfile(
                entity.getId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getProfileImageUrl(),
                entity.getPasswordHash()
        );
    }
}
