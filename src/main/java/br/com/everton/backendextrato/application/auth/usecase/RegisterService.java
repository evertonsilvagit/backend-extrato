package br.com.everton.backendextrato.application.auth.usecase;

import br.com.everton.backendextrato.application.auth.port.in.RegisterUseCase;
import br.com.everton.backendextrato.application.auth.port.out.AuthTokenPort;
import br.com.everton.backendextrato.application.auth.port.out.PasswordHashPort;
import br.com.everton.backendextrato.application.auth.port.out.UserProfileRepository;
import br.com.everton.backendextrato.application.auth.usecase.command.AuthCommand;
import br.com.everton.backendextrato.application.auth.usecase.result.AuthSession;
import br.com.everton.backendextrato.domain.auth.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterService extends AuthValidationSupport implements RegisterUseCase {

    private final UserProfileRepository userProfileRepository;
    private final PasswordHashPort passwordHashPort;
    private final AuthTokenPort authTokenPort;

    public RegisterService(
            UserProfileRepository userProfileRepository,
            PasswordHashPort passwordHashPort,
            AuthTokenPort authTokenPort
    ) {
        this.userProfileRepository = userProfileRepository;
        this.passwordHashPort = passwordHashPort;
        this.authTokenPort = authTokenPort;
    }

    @Override
    @Transactional
    public AuthSession execute(AuthCommand command) {
        String email = requireEmail(command == null ? null : command.email());
        String password = requirePassword(command == null ? null : command.password());
        String displayName = requireDisplayName(command == null ? null : command.name());
        String photo = normalizePhoto(command == null ? null : command.photo());

        if (userProfileRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este email.");
        }

        UserProfile saved = userProfileRepository.save(
                new UserProfile(null, email, displayName, photo, passwordHashPort.encode(password))
        );
        return toSession(saved, authTokenPort.issue(saved));
    }
}
