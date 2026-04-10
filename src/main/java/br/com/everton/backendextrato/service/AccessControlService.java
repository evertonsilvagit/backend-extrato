package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.AccessManagementResponse;
import br.com.everton.backendextrato.dto.AccessibleOwnerResponse;
import br.com.everton.backendextrato.dto.ShareAccessRequest;
import br.com.everton.backendextrato.dto.SharedViewerResponse;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.model.UserAccount;
import br.com.everton.backendextrato.model.UserDataAccess;
import br.com.everton.backendextrato.repository.UserAccountRepository;
import br.com.everton.backendextrato.repository.UserDataAccessRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AccessControlService {

    private final UserDataAccessRepository userDataAccessRepository;
    private final UserAccountRepository userAccountRepository;

    public AccessControlService(
            UserDataAccessRepository userDataAccessRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.userDataAccessRepository = userDataAccessRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.ACCESS_MANAGEMENT, key = "#currentUserEmail")
    public AccessManagementResponse getManagementData(String currentUserEmail) {
        String normalizedEmail = normalizeEmail(currentUserEmail);

        List<SharedViewerResponse> viewers = userDataAccessRepository
                .findAllByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(normalizedEmail)
                .stream()
                .map(this::toSharedViewerResponse)
                .toList();

        List<AccessibleOwnerResponse> accessibleOwners = new ArrayList<>();
        UserAccount currentUser = requireUser(normalizedEmail);
        accessibleOwners.add(toAccessibleOwnerResponse(currentUser, true));

        userDataAccessRepository.findAllByViewerEmailIgnoreCaseOrderByCreatedAtDesc(normalizedEmail)
                .stream()
                .map(UserDataAccess::getOwnerEmail)
                .distinct()
                .map(this::requireUser)
                .map(user -> toAccessibleOwnerResponse(user, false))
                .sorted(Comparator.comparing(AccessibleOwnerResponse::name, String.CASE_INSENSITIVE_ORDER))
                .forEach(accessibleOwners::add);

        return new AccessManagementResponse(viewers, accessibleOwners);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.ACCESS_MANAGEMENT, allEntries = true)
    public SharedViewerResponse grantAccess(String ownerEmail, ShareAccessRequest request) {
        if (request == null || request.viewerEmail() == null || request.viewerEmail().isBlank()) {
            throw new IllegalArgumentException("Informe o email do usuário que receberá acesso.");
        }

        String normalizedOwnerEmail = normalizeEmail(ownerEmail);
        String normalizedViewerEmail = normalizeEmail(request.viewerEmail());

        if (normalizedOwnerEmail.equalsIgnoreCase(normalizedViewerEmail)) {
            throw new IllegalArgumentException("Você já tem acesso aos seus próprios dados.");
        }

        requireUser(normalizedOwnerEmail);
        requireUser(normalizedViewerEmail);

        if (userDataAccessRepository.existsByOwnerEmailIgnoreCaseAndViewerEmailIgnoreCase(normalizedOwnerEmail, normalizedViewerEmail)) {
            throw new IllegalArgumentException("Esse usuário já pode visualizar seus dados.");
        }

        UserDataAccess access = new UserDataAccess();
        access.setOwnerEmail(normalizedOwnerEmail);
        access.setViewerEmail(normalizedViewerEmail);

        return toSharedViewerResponse(userDataAccessRepository.save(access));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.ACCESS_MANAGEMENT, allEntries = true)
    public void revokeAccess(String ownerEmail, Long accessId) {
        UserDataAccess access = userDataAccessRepository.findByIdAndOwnerEmailIgnoreCase(accessId, normalizeEmail(ownerEmail))
                .orElseThrow(() -> new IllegalArgumentException("Compartilhamento não encontrado."));
        userDataAccessRepository.delete(access);
    }

    @Transactional(readOnly = true)
    public String resolveReadableOwner(String currentUserEmail, String requestedOwnerEmail) {
        String normalizedCurrentUserEmail = normalizeEmail(currentUserEmail);
        if (requestedOwnerEmail == null || requestedOwnerEmail.isBlank()) {
            return normalizedCurrentUserEmail;
        }

        String normalizedRequestedOwnerEmail = normalizeEmail(requestedOwnerEmail);
        if (normalizedCurrentUserEmail.equalsIgnoreCase(normalizedRequestedOwnerEmail)) {
            return normalizedCurrentUserEmail;
        }

        boolean allowed = userDataAccessRepository.existsByOwnerEmailIgnoreCaseAndViewerEmailIgnoreCase(
                normalizedRequestedOwnerEmail,
                normalizedCurrentUserEmail
        );

        if (!allowed) {
            throw new IllegalArgumentException("Você não tem permissão para visualizar os dados desse usuário.");
        }

        requireUser(normalizedRequestedOwnerEmail);
        return normalizedRequestedOwnerEmail;
    }

    @Transactional(readOnly = true)
    public String resolveWritableOwner(String currentUserEmail, String requestedOwnerEmail) {
        String normalizedCurrentUserEmail = normalizeEmail(currentUserEmail);
        if (requestedOwnerEmail == null || requestedOwnerEmail.isBlank()) {
            return normalizedCurrentUserEmail;
        }

        String normalizedRequestedOwnerEmail = normalizeEmail(requestedOwnerEmail);
        if (normalizedCurrentUserEmail.equalsIgnoreCase(normalizedRequestedOwnerEmail)) {
            return normalizedCurrentUserEmail;
        }

        boolean allowed = userDataAccessRepository.existsByOwnerEmailIgnoreCaseAndViewerEmailIgnoreCase(
                normalizedRequestedOwnerEmail,
                normalizedCurrentUserEmail
        );

        if (!allowed) {
            throw new IllegalArgumentException("Você não tem permissão para alterar os dados desse usuário.");
        }

        requireUser(normalizedRequestedOwnerEmail);
        return normalizedRequestedOwnerEmail;
    }

    private SharedViewerResponse toSharedViewerResponse(UserDataAccess access) {
        UserAccount viewer = requireUser(access.getViewerEmail());
        return new SharedViewerResponse(
                access.getId(),
                viewer.getEmail(),
                viewer.getDisplayName(),
                viewer.getProfileImageUrl(),
                access.getCreatedAt()
        );
    }

    private AccessibleOwnerResponse toAccessibleOwnerResponse(UserAccount userAccount, boolean own) {
        return new AccessibleOwnerResponse(
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getProfileImageUrl(),
                own
        );
    }

    private UserAccount requireUser(String email) {
        return userAccountRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + email));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email é obrigatório.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
