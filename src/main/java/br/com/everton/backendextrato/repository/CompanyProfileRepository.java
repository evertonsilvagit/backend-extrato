package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    Optional<CompanyProfile> findByUserEmailIgnoreCase(String userEmail);
}
