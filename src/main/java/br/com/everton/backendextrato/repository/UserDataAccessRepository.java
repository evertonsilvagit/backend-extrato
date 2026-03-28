package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.UserDataAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDataAccessRepository extends JpaRepository<UserDataAccess, Long> {
    List<UserDataAccess> findAllByOwnerEmailIgnoreCaseOrderByCreatedAtDesc(String ownerEmail);
    List<UserDataAccess> findAllByViewerEmailIgnoreCaseOrderByCreatedAtDesc(String viewerEmail);
    boolean existsByOwnerEmailIgnoreCaseAndViewerEmailIgnoreCase(String ownerEmail, String viewerEmail);
    Optional<UserDataAccess> findByIdAndOwnerEmailIgnoreCase(Long id, String ownerEmail);
}
