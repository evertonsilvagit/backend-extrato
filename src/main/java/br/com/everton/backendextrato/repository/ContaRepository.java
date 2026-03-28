package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {
    List<Conta> findAllByUserEmailIgnoreCase(String userEmail);
    Optional<Conta> findByIdAndUserEmailIgnoreCase(Long id, String userEmail);
    boolean existsByIdAndUserEmailIgnoreCase(Long id, String userEmail);
}
