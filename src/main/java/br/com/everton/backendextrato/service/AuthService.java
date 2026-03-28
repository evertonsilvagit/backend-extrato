package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthTokenService;
import br.com.everton.backendextrato.dto.AuthRequest;
import br.com.everton.backendextrato.dto.AuthResponse;
import br.com.everton.backendextrato.dto.ProfileResponse;
import br.com.everton.backendextrato.dto.ProfileUpdateRequest;
import br.com.everton.backendextrato.model.UserAccount;
import br.com.everton.backendextrato.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        String email = requireEmail(request);
        String password = requirePassword(request);
        String displayName = requireDisplayName(request == null ? null : request.name());
        String photo = normalizePhoto(request == null ? null : request.photo());
        if (userAccountRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este email.");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(email);
        userAccount.setDisplayName(displayName);
        userAccount.setProfileImageUrl(photo);
        userAccount.setPasswordHash(passwordEncoder.encode(password));

        UserAccount saved = userAccountRepository.save(userAccount);
        return issueToken(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        String email = requireEmail(request);
        String password = requirePassword(request);

        UserAccount userAccount = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos."));

        if (!passwordEncoder.matches(password, userAccount.getPasswordHash())) {
            throw new IllegalArgumentException("Email ou senha inválidos.");
        }

        return issueToken(userAccount);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        UserAccount userAccount = userAccountRepository.findByEmailIgnoreCase(requireEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        return toProfileResponse(userAccount);
    }

    @Transactional
    public AuthResponse updateProfile(String email, ProfileUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do perfil são obrigatórios.");
        }

        UserAccount userAccount = userAccountRepository.findByEmailIgnoreCase(requireEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        userAccount.setDisplayName(requireDisplayName(request.name()));
        userAccount.setProfileImageUrl(normalizePhoto(request.photo()));

        UserAccount saved = userAccountRepository.save(userAccount);
        return issueToken(saved);
    }

    private AuthResponse issueToken(UserAccount userAccount) {
        String token = authTokenService.generateToken(
                new AuthenticatedUser(userAccount.getId(), userAccount.getEmail(), userAccount.getDisplayName())
        );
        return new AuthResponse(
                token,
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getProfileImageUrl()
        );
    }

    private ProfileResponse toProfileResponse(UserAccount userAccount) {
        return new ProfileResponse(
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getProfileImageUrl()
        );
    }

    private String requireEmail(AuthRequest request) {
        return requireEmail(request == null ? null : request.email());
    }

    private String requireEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório.");
        }
        return email.trim().toLowerCase();
    }

    private String requirePassword(AuthRequest request) {
        if (request == null || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }
        return request.password();
    }
    private String requireDisplayName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres.");
        }

        if (trimmedName.length() > 120) {
            throw new IllegalArgumentException("Nome deve ter no máximo 120 caracteres.");
        }

        return trimmedName;
    }

    private String normalizePhoto(String photo) {
        if (photo == null || photo.isBlank()) {
            return null;
        }

        String trimmedPhoto = photo.trim();
        if (!trimmedPhoto.startsWith("data:image/")) {
            throw new IllegalArgumentException("Foto inválida. Envie uma imagem válida.");
        }

        if (trimmedPhoto.length() > 2_000_000) {
            throw new IllegalArgumentException("Foto muito grande. Escolha uma imagem menor.");
        }

        return trimmedPhoto;
    }
}
