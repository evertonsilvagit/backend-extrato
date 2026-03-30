package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.CategoriaDivida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaDividaRepository extends JpaRepository<CategoriaDivida, Long> {
    List<CategoriaDivida> findAllByUserEmailIgnoreCaseOrderByNomeAsc(String userEmail);
    Optional<CategoriaDivida> findByIdAndUserEmailIgnoreCase(Long id, String userEmail);
    Optional<CategoriaDivida> findByUserEmailIgnoreCaseAndNomeIgnoreCase(String userEmail, String nome);
}
