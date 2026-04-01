package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.FinancialSeparationWorkspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FinancialSeparationWorkspaceRepository extends JpaRepository<FinancialSeparationWorkspace, Long> {
    Optional<FinancialSeparationWorkspace> findByUserEmailIgnoreCase(String userEmail);
}
