package br.com.everton.backendextrato.application.auth.usecase;

import br.com.everton.backendextrato.application.auth.port.in.GetProfileUseCase;
import br.com.everton.backendextrato.application.auth.port.out.UserProfileRepository;
import br.com.everton.backendextrato.domain.auth.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetProfileService extends AuthValidationSupport implements GetProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    public GetProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile execute(String email) {
        return userProfileRepository.findByEmail(requireEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }
}
