package br.com.everton.backendextrato.application.auth.usecase;

import br.com.everton.backendextrato.application.auth.port.in.LoginUseCase;
import br.com.everton.backendextrato.application.auth.port.out.AuthTokenPort;
import br.com.everton.backendextrato.application.auth.port.out.PasswordHashPort;
import br.com.everton.backendextrato.application.auth.port.out.UserProfileRepository;
import br.com.everton.backendextrato.application.auth.usecase.command.AuthCommand;
import br.com.everton.backendextrato.application.auth.usecase.result.AuthSession;
import br.com.everton.backendextrato.domain.auth.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService extends AuthValidationSupport implements LoginUseCase {

    private final UserProfileRepository userProfileRepository;
    private final PasswordHashPort passwordHashPort;
    private final AuthTokenPort authTokenPort;

    public LoginService(
            UserProfileRepository userProfileRepository,
            PasswordHashPort passwordHashPort,
            AuthTokenPort authTokenPort
    ) {
        this.userProfileRepository = userProfileRepository;
        this.passwordHashPort = passwordHashPort;
        this.authTokenPort = authTokenPort;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthSession execute(AuthCommand command) {
        String email = requireEmail(command == null ? null : command.email());
        String password = requirePassword(command == null ? null : command.password());

        UserProfile userProfile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos."));

        if (!passwordHashPort.matches(password, userProfile.passwordHash())) {
            throw new IllegalArgumentException("Email ou senha inválidos.");
        }

        return toSession(userProfile, authTokenPort.issue(userProfile));
    }
}
