package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.Entrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EntradaRepository extends JpaRepository<Entrada, Long> {
    Optional<Entrada> findByIdAndUserEmailIgnoreCase(Long id, String userEmail);
    Page<Entrada> findAllByUserEmailIgnoreCase(String userEmail, Pageable pageable);
    boolean existsByIdAndUserEmailIgnoreCase(Long id, String userEmail);
}
