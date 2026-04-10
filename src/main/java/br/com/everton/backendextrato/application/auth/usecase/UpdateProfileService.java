package br.com.everton.backendextrato.application.auth.usecase;

import br.com.everton.backendextrato.application.auth.port.in.UpdateProfileUseCase;
import br.com.everton.backendextrato.application.auth.port.out.AuthTokenPort;
import br.com.everton.backendextrato.application.auth.port.out.UserProfileRepository;
import br.com.everton.backendextrato.application.auth.usecase.command.UpdateProfileCommand;
import br.com.everton.backendextrato.application.auth.usecase.result.AuthSession;
import br.com.everton.backendextrato.domain.auth.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateProfileService extends AuthValidationSupport implements UpdateProfileUseCase {

    private final UserProfileRepository userProfileRepository;
    private final AuthTokenPort authTokenPort;

    public UpdateProfileService(UserProfileRepository userProfileRepository, AuthTokenPort authTokenPort) {
        this.userProfileRepository = userProfileRepository;
        this.authTokenPort = authTokenPort;
    }

    @Override
    @Transactional
    public AuthSession execute(String email, UpdateProfileCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Dados do perfil são obrigatórios.");
        }

        UserProfile userProfile = userProfileRepository.findByEmail(requireEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        UserProfile saved = userProfileRepository.save(
                new UserProfile(
                        userProfile.id(),
                        userProfile.email(),
                        requireDisplayName(command.name()),
                        normalizePhoto(command.photo()),
                        userProfile.passwordHash()
                )
        );

        return toSession(saved, authTokenPort.issue(saved));
    }
}
