package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.CategoriaConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaContaRepository extends JpaRepository<CategoriaConta, Long> {
    List<CategoriaConta> findAllByUserEmailIgnoreCaseOrderByNomeAsc(String userEmail);
    Optional<CategoriaConta> findByIdAndUserEmailIgnoreCase(Long id, String userEmail);
    Optional<CategoriaConta> findByUserEmailIgnoreCaseAndNomeIgnoreCase(String userEmail, String nome);
}
