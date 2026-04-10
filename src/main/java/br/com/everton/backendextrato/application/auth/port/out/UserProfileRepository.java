package br.com.everton.backendextrato.application.auth.port.out;

import br.com.everton.backendextrato.domain.auth.UserProfile;

import java.util.Optional;

public interface UserProfileRepository {
    Optional<UserProfile> findByEmail(String email);
    UserProfile save(UserProfile userProfile);
}
